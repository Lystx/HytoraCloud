package cloud.hytora.node.impl.module;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.ModuleClassLoader;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.module.controller.base.*;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.module.controller.task.ScheduledModuleTask;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class DefaultModuleController implements ModuleController {

    private final IModuleManager manager;
    private final Path jarFile;
    private final ClassLoader mainClassLoader;
    private final Consumer<ModuleClassLoader> unregisterClassLoader;

    private Path dataFolder;
    private StorableDocument config;
    private ModuleClassLoader classLoader;
    private ModuleConfig moduleConfig;
    private AbstractModule module;

    private ModuleState state = ModuleState.DISABLED;

    private final Map<Object, Collection<HandlerMethod<ModuleTask>>> moduleTasks;

    public DefaultModuleController(ClassLoader mainClassLoader, @Nonnull IModuleManager manager, @Nonnull Path jarFile, Consumer<ModuleClassLoader> unregisterClassLoader) {
        this.mainClassLoader = mainClassLoader;
        this.manager = manager;
        this.jarFile = jarFile;
        this.unregisterClassLoader = unregisterClassLoader;

        this.moduleTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isInitialized() {
        return module != null;
    }

    public void initConfig() throws Exception {
        URL url = jarFile.toUri().toURL();

        this.classLoader = new ModuleClassLoader(Arrays.asList(url).toArray(new URL[0]), this.mainClassLoader, jarFile.toFile());

        Document document = classLoader.loadDocument("config.json");

        if (document == null || document.isEmpty()) {
            Logger.constantInstance().error("Missing 'config.json' for module with file {}", jarFile.toString());
            return;
        } else {
            if (!document.contains("name")) throw new IllegalArgumentException("Missing property 'name'");
            if (!document.contains("version")) throw new IllegalArgumentException("Missing property 'version'");
            if (!document.contains("author")) throw new IllegalArgumentException("Missing property 'author'");
            if (!document.contains("main")) throw new IllegalArgumentException("Missing property 'main'");

            this.moduleConfig = new ModuleConfig(
                    document.getString("name"),
                    document.getString("description", ""),
                    document.getString("version"),
                    document.getString("main"),
                    document.getString("website", ""),
                    new String[]{document.getString("author")},
                    document.getStrings("depends").toArray(new String[0]),
                    document.getEnum("copy", ModuleCopyType.NONE),
                    document.getEnum("environment", ModuleEnvironment.ALL)
            );
        }

        dataFolder = manager.getModulesDirectory().resolve(moduleConfig.getName());
        dataFolder.toFile().mkdirs();

        Document config = DocumentFactory.newJsonDocument();
        if (getConfig().isEmpty()) {
            config.saveToFile(dataFolder.resolve("config.json"));
        }
    }


    public void initModule() throws Exception {

        Class<?> mainClass = classLoader
                .loadClass(
                        moduleConfig
                                .getMainClass()
                );
        if (mainClass == null) {
            Logger.constantInstance().error("Couldn't initialize Module[name={}, version={}, main={}] because main class was not found!", moduleConfig.getName(), moduleConfig.getVersion(), moduleConfig.getMainClass());
            return;
        }
        Constructor<?> constructor = mainClass.getDeclaredConstructor(ModuleController.class);

        Object instance = constructor.newInstance(this);

        AbstractModule abstractModule = new AbstractModule();
        abstractModule.setController(this);
        abstractModule.setHttpServer(NodeDriver.getInstance().getWebServer());
        this.registerModuleTasks(instance);

        classLoader.setModule((module = abstractModule));

    }

    @Override
    public void loadModule() {
        synchronized (this) {
            if (module == null) return; // was never initialized
            if (state != ModuleState.DISABLED && state != ModuleState.UNREGISTERED ) return; // must be disabled first

            this.reloadConfig();
            Logger.constantInstance().info("§e=> §fModule §b" + module + " §7is being loaded§8...");
            try {

                state = ModuleState.LOADED;
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                Logger.constantInstance().error("An error occurred while loading module " + module);
                ex.printStackTrace();
                disableModule();
            }
        }
    }

    @Override
    public void enableModule() {
        synchronized (this) {
            if (module == null) return; // was never initialized
            if (state != ModuleState.LOADED) return; // must be loaded first

            Logger.constantInstance().info("§a=> §fModule §b" + module + " §7is being enabled§8...");

            try {
                state = ModuleState.ENABLED;
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                Logger.constantInstance().error("An error occurred while enabling module " + module);
                ex.printStackTrace();
                disableModule();
            }
        }
    }

    @Override
    public void disableModule() {
        synchronized (this) {
            if (module == null) return; // Was never initialized
            if (state == ModuleState.DISABLED) return; // Is already disabled

            Logger.constantInstance().info("§c=> §fModule " + module + " §7is being disabled§8...");

            try {
                state = ModuleState.DISABLED;
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                Logger.constantInstance().error("An error occurred while disabling module " + module);
                ex.printStackTrace();
            }

            state = ModuleState.DISABLED;
            this.unregisterClassLoader.accept(classLoader);
        }
    }

    @Override
    public void unregisterModule() {
        synchronized (this) {

            try {
                classLoader.close();
            } catch (Exception ex) {
                Logger.constantInstance().error("Unable to close classloader");
                ex.printStackTrace();
            }

            state = ModuleState.UNREGISTERED;
        }
    }


    @Override
    public void registerModuleTasks(Object objectClass) {
        List<HandlerMethod<ModuleTask>> moduleTasks = new ArrayList<>();

        for (Method m : objectClass.getClass().getDeclaredMethods()) {
            ModuleTask annotation = m.getAnnotation(ModuleTask.class);
            ScheduledModuleTask scheduledModuleTask = m.getAnnotation(ScheduledModuleTask.class);

            if (annotation != null) {
                HandlerMethod<ModuleTask> moduleTaskHandlerMethod = new HandlerMethod<>(objectClass, m, Void.class, annotation);
                if (scheduledModuleTask != null) {
                    moduleTaskHandlerMethod.setObjects(new Object[]{scheduledModuleTask});
                }
                moduleTasks.add(moduleTaskHandlerMethod);
            }
        }

        moduleTasks.sort(Comparator.comparingInt(em -> em.getAnnotation().id()));
        this.moduleTasks.put(objectClass, moduleTasks);
    }

    public void callTasks(ModuleState state) {
        this.moduleTasks.forEach((object, handlers) -> {
            for (HandlerMethod<ModuleTask> em : handlers) {
                if (em.getObjects() != null && em.getObjects()[0] instanceof ScheduledModuleTask) {
                    ScheduledModuleTask scheduledModuleTask = (ScheduledModuleTask) em.getObjects()[0];
                    Scheduler scheduler = Scheduler.runTimeScheduler();

                    long delay = scheduledModuleTask.delay();
                    boolean sync = scheduledModuleTask.sync();
                    long repeat = scheduledModuleTask.repeat();

                    if (repeat != -1) {
                        if (sync) {
                            scheduler.scheduleRepeatingTask(() -> this.subExecute(em, state), delay, repeat);
                        } else {
                            scheduler.scheduleRepeatingTaskAsync(() -> this.subExecute(em, state), delay, repeat);
                        }
                    } else {
                        if (sync) {
                            scheduler.scheduleDelayedTask(() -> this.subExecute(em, state), delay);
                        } else {
                            scheduler.scheduleDelayedTaskAsync(() -> this.subExecute(em, state), delay);
                        }
                    }
                } else {
                    this.subExecute(em, state);
                }
            }
        });
    }

    private void subExecute(HandlerMethod<ModuleTask> em, ModuleState state) {

        if (em.getAnnotation().state() == state) {
            try {
                em.getMethod().invoke(em.getListener());
            } catch (IllegalAccessException | InvocationTargetException e) {
                //ignoring on shutdown
            }
        }
    }

    @Nonnull
    @Override
    public StorableDocument getConfig() {
        if (config == null) return reloadConfig();
        return config;
    }

    @Nonnull
    @Override
    public StorableDocument reloadConfig() {
        synchronized (this) {
            return config = DocumentFactory.newStorableJsonDocumentUnchecked(this.getDataFolder().resolve("config.json"));
        }
    }


    @Override
    public boolean isEnabled() {
        return module == null ? !getConfig().contains("enabled") || getConfig().getBoolean("enabled") : isEnabled(true);
    }

    @Override
    public boolean isEnabled(boolean defaultValue) {
        StorableDocument config = getConfig();
        if (!config.contains("enabled")) {
            return false;
        }

        boolean enabled = config.getBoolean("enabled");
        CloudDriver.getInstance().getLogger().debug("{} Status Check: enabled={}", getModuleConfig().getFullName(), enabled);
        return enabled;
    }

    @Nonnull
    @Override
    public ModuleState getState() {
        return state;
    }

    @Nonnull
    @Override
    public Path getJarFile() {
        return jarFile;
    }

    @Nonnull
    @Override
    public Path getDataFolder() {
        return dataFolder;
    }

    @Nonnull
    @Override
    public IModuleManager getManager() {
        return manager;
    }

    @Nonnull
    @Override
    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    @Nonnull
    @Override
    public AbstractModule getModule() {
        return module;
    }

    @Nonnull
    @Override
    public ModuleClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        return "ModuleController[file=" + jarFile.getFileName() + "]";
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        if (state == BufferState.WRITE) {
            buf.writeBoolean(isEnabled());
            buf.writeBoolean(isInitialized());

            buf.writeEnum(state);
            buf.writeObject(moduleConfig);

            buf.writeDocument(config);
        }
    }
}
