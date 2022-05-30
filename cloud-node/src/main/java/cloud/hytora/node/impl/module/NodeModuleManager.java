package cloud.hytora.node.impl.module;


import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.networking.packets.module.RemoteModuleExecutionPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.networking.protocol.wrapped.ChanneledPacketAction;
import cloud.hytora.node.NodeDriver;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class NodeModuleManager implements ModuleManager {

    private List<DefaultModuleController> modules = Collections.emptyList();
    private Path directory;

    @Nonnull
    public Path getModulesDirectory() {
        return directory;
    }

    @Override
    public void setModulesDirectory(@Nonnull Path directory) {
        FileUtils.createDirectory(directory);
        this.directory = directory;
    }

    @Override
    public synchronized void unregisterModules() {
        for (DefaultModuleController module : modules) {
            try {
                module.unregisterModule();
            } catch (Throwable ex) {
                CloudDriver.getInstance().getLogger().error("An error occurred while closing class loader", ex);
            }
        }
        modules.clear();

        if (NodeDriver.getInstance().getConfig().isRemote()) { //deleting so can be received
            FileUtils.delete(NodeDriver.MODULE_FOLDER.toPath());
        }
    }


    @Override
    public synchronized void resolveModules() {
        unregisterModules();
        CloudDriver.getInstance().getLogger().info("Resolving modules..");
        FileUtils.createDirectory(directory);

        if (NodeDriver.getInstance().getConfig().isRemote()) {
            //is remote
            PacketBuffer buffer = NodeDriver.getInstance()
                    .getExecutor()
                    .getNodeAsClient()
                    .getWrapper()
                    .prepareSingleQuery()
                    .execute(new RemoteModuleExecutionPacket(RemoteModuleExecutionPacket.PayLoad.TRANSFER_MODULES))
                    .syncUninterruptedly()
                    .get()
                    .buffer();

            int moduleAmount = buffer.readInt();
            CloudDriver.getInstance().getLogger().info("Downloading {} Modules from HeadNode...", moduleAmount);

            for (int i = 0; i < moduleAmount; i++) {
                try {
                    String jarName = buffer.readString();
                    String folderName = buffer.readString();

                    File destination = new File(NodeDriver.MODULE_FOLDER, jarName);
                    File folder = new File(NodeDriver.MODULE_FOLDER, folderName + "/");
                    folder.mkdirs();

                    File jarFile = buffer.readFile(destination);
                    File dataFolder = buffer.readFile(folder);
                    CloudDriver.getInstance().getLogger().info("Downloaded Module " + jarName + " from HeadNode!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<DefaultModuleController> modules = new CopyOnWriteArrayList<>();

        // resolve modules and load configs
        for (Path file : FileUtils.list(directory).filter(path -> path.toString().endsWith(".jar")).collect(Collectors.toList())) {
            try {
                CloudDriver.getInstance().getLogger().info("Resolving module {}..", file.getFileName());
                Path selfBasePath = new File(CloudDriver.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();

                DefaultModuleController module = new DefaultModuleController(CloudDriver.class.getClassLoader(), this, file, selfBasePath, (loggingType, message) -> {
                    switch (loggingType) {
                        case "INFO":
                            CloudDriver.getInstance().getLogger().info(message);
                            break;
                        case "ERROR":
                            CloudDriver.getInstance().getLogger().error(message);
                            break;
                        case "DEBUG":
                            CloudDriver.getInstance().getLogger().debug(message);
                            break;
                        case "WARN":
                            CloudDriver.getInstance().getLogger().warn(message);
                            break;
                        case "TRACE":
                            CloudDriver.getInstance().getLogger().trace(message);
                            break;
                    }
                }, moduleClassLoader -> CloudDriver.getInstance().getEventManager().unregisterListeners(moduleClassLoader));
                module.initConfig();

                modules.add(module);
            } catch (Throwable ex) {
                CloudDriver.getInstance().getLogger().error("Could not resolve module {}", FileUtils.getRealFileName(file), ex);
            }
        }

        // check if the depends are existing
        for (DefaultModuleController module : modules) {
            for (String depend : module.getModuleConfig().getDepends()) {
                if (depend.trim().isEmpty()) {
                    continue;
                }
                if (hasModule(modules, depend)) continue;

                modules.remove(module);
                CloudDriver.getInstance().getLogger().error("Could not find required depend '{}' for module {}", depend, module.getModuleConfig());
            }
        }

        // order the modules by depends
        // TODO handle: a requires b, b requires c, c requires a -> invalid
        modules.sort((module1, module2) -> {

            // if this module requires the other module, load this after the other
            if (arrayContains(module1.getModuleConfig().getDepends(), module2.getModuleConfig().getName()))
                return -1;

            // if the other module requires this module, load the other after this
            if (arrayContains(module2.getModuleConfig().getDepends(), module1.getModuleConfig().getName()))
                return 1;

            return 0;
        });

        // init modules
        for (DefaultModuleController module : modules) {
            try {
                if (!module.getModuleConfig().getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    CloudDriver.getInstance().getLogger().info("Skipping initialization of {} (ModuleEnvironment.{}, DriverEnvironment.{})",
                            module, module.getModuleConfig().getEnvironment(), CloudDriver.getInstance().getEnvironment());
                    continue;
                }

                module.initModule();
            } catch (Throwable ex) {
                modules.remove(module);
                CloudDriver.getInstance().getLogger().error("An error occurred while initializing {}", module.getModuleConfig(), ex);
            }
        }

        this.modules = modules;
    }

    private boolean hasModule(@Nonnull Collection<DefaultModuleController> modules, @Nonnull String depend) {
        for (DefaultModuleController module : modules) {
            if (module.getModuleConfig().getName().equals(depend))
                return true;
        }
        return false;
    }

    private <T> boolean arrayContains(@Nonnull T[] array, T subject) {
        for (T t : array) {
            if (t.equals(subject))
                return true;
        }
        return false;
    }

    @Override
    public synchronized void loadModules() {
        for (ModuleController module : modules) {
            module.loadModule();
        }
    }

    @Override
    public synchronized void enableModules() {
        for (ModuleController module : modules) {
            module.enableModule();
        }
    }

    @Override
    public synchronized void disableModules() {
        for (ModuleController module : modules) {
            module.disableModule();
        }
    }

    @Nonnull
    @Override
    public List<ModuleController> getModules() {
        return Collections.unmodifiableList(modules);
    }

}
