package cloud.hytora.node.impl.module;


import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.IdentifiableClassLoader;
import cloud.hytora.driver.common.exception.HytoraCloudException;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class NodeModuleManager implements ModuleManager {

    private final List<DefaultModuleController> modules = new ArrayList<>();
    private Path directory;

    @Nonnull
    public Path getModulesDirectory() {
        return directory;
    }

    @Override
    public void setModulesDirectory(@Nonnull Path directory) {
        Logger.constantInstance().debug("Set Module-Loading-Directory to {}!", directory.toString());
        FileUtils.createDirectory(directory);
        this.directory = directory;
    }


    @Override
    public void removeModule(ModuleController moduleController) {
        DefaultModuleController defaultModuleController = this.modules.stream().filter(m -> m.getModuleConfig().getName().equalsIgnoreCase(moduleController.getModuleConfig().getName()))
                .findFirst().orElse(null);
        if (defaultModuleController == null) {
            return;
        }
        this.modules.remove(defaultModuleController);
    }

    @Override
    public void unregisterModules() {
        Logger.constantInstance().debug("Unregistering modules [{}]...", modules.size());
        for (DefaultModuleController module : modules) {
            try {
                module.unregisterModule();
            } catch (Throwable ex) {
                CloudDriver.getInstance().getLogger().error("An error occurred while closing class loader", ex);
            }
        }
        modules.clear();

    }

    public ModuleController resolveModule(Path file) {

        try {
            CloudDriver.getInstance().getLogger().debug("Resolving the module §8'%1{}§8'§f...", file.getFileName());
            Path selfBasePath = new File(CloudDriver.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (!(classLoader instanceof IdentifiableClassLoader)) {
                throw new HytoraCloudException("Wrong SystemClassLoader : " + classLoader.getClass().getName());
            }

            DefaultModuleController module = new DefaultModuleController(classLoader, this, file, moduleClassLoader -> CloudDriver.getInstance().getEventManager().unregisterListeners(moduleClassLoader));
            module.initConfig();

            return module;
        } catch (Throwable ex) {
            CloudDriver.getInstance().getLogger().error("Could not resolve module {}", FileUtils.getRealFileName(file), ex);
        }
        return null;
    }

    @Override
    public void resolveModules() {
        Logger.constantInstance().debug("Resolving Modules...");
        FileUtils.createDirectory(directory);

        List<DefaultModuleController> modules = new ArrayList<>();

        // resolve modules and load configs
        for (Path file : FileUtils.list(directory).filter(path -> path.toString().endsWith(".jar")).collect(Collectors.toList())) {
            if (getModules().stream().anyMatch(m -> m.getJarFile().equals(file))) {
                continue;
            }
            modules.add((DefaultModuleController) resolveModule(file));
        }

        // check if the depends are existing
        for (DefaultModuleController module : modules) {
            if (module.getModuleConfig() == null) {
                CloudDriver.getInstance().getLogger().error("Missing 'config.json' for module!");
                continue;
            }
            if (this.getModule(module.getModuleConfig().getName()) != null) {
                //if already loaded skip...
                continue;
            }
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
        // TODO check if this is right => : a requires b, b requires c, c requires a -> invalid
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
            Logger.constantInstance().debug("Initializing Module {}", module.getModuleConfig().getFullName());
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

        for (DefaultModuleController module : modules) {
            this.modules.add(module);
        }
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
    public void loadModules() {
        Logger.constantInstance().debug("Loading Modules [{}]...", modules.size());
        for (ModuleController module : modules) {
            module.loadModule();
        }
    }

    @Override
    public  void enableModules() {
        Logger.constantInstance().debug("Enabling Modules [{}]...", modules.size());
        for (ModuleController module : modules) {
            module.enableModule();
        }
    }

    @Override
    public void disableModules() {
        Logger.constantInstance().debug("Disabling Modules [{}]...", modules.size());
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
