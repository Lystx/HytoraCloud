package cloud.hytora.node.impl.module;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.ModuleClassLoader;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.module.controller.task.ScheduledModuleTask;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class DefaultModuleController implements ModuleController {

    protected static final String CONFIG_RESOURCE = "module.json";

    private final ModuleManager manager;
    private final Path jarFile, selfJar;
    private final ClassLoader mainClassLoader;
    private final BiConsumer<String, String> logger;
    private final Consumer<ModuleClassLoader> unregisterClassLoader;

    private Path dataFolder;
    private StorableDocument config;
    private ModuleClassLoader classLoader;
    private ModuleConfig moduleConfig;
    private DriverModule module;

    private ModuleState state = ModuleState.DISABLED;

    private final Map<Object, Collection<HandlerMethod<ModuleTask>>> moduleTasks;

    public DefaultModuleController(ClassLoader mainClassLoader, @Nonnull ModuleManager manager, @Nonnull Path jarFile, @Nonnull Path selfJar, BiConsumer<String, String> logger, Consumer<ModuleClassLoader> unregisterClassLoader) {
        this.mainClassLoader = mainClassLoader;
        this.manager = manager;
        this.jarFile = jarFile;
        this.selfJar = selfJar;
        this.logger = logger;
        this.unregisterClassLoader = unregisterClassLoader;

        this.moduleTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isInitialized() {
        return module != null;
    }

    public void initConfig() throws Exception {

        URL url = jarFile.toUri().toURL();
        URL selfUrl = selfJar.toUri().toURL();

        classLoader = new ModuleClassLoader(Arrays.asList(url, selfUrl).toArray(new URL[0]), this.mainClassLoader);

        InputStream input = classLoader.getResourceAsStream(CONFIG_RESOURCE);
        if (input == null) throw new IllegalArgumentException("No such resource " + CONFIG_RESOURCE);

        InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        Document document;

        try {
            document = DocumentFactory.newJsonDocument(reader);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse module config", ex);
        }

        if (!document.contains("name")) throw new IllegalArgumentException("Missing property 'name'");
        if (!document.contains("version")) throw new IllegalArgumentException("Missing property 'version'");
        if (!document.contains("author")) throw new IllegalArgumentException("Missing property 'author'");
        if (!document.contains("main")) throw new IllegalArgumentException("Missing property 'main'");

        moduleConfig = new ModuleConfig(
                document.getString("name"),
                document.getString("author"),
                document.getString("description", ""),
                document.getString("version"),
                document.getString("main"),
                document.getString("website", ""),
                document.getStrings("depends").toArray(new String[0]),
                document.getEnum("copy", ModuleCopyType.NONE),
                document.getEnum("environment", ModuleEnvironment.ALL)
        );

        dataFolder = manager.getModulesDirectory().resolve(moduleConfig.getName());
        dataFolder.toFile().mkdirs();
    }

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
                    Scheduler scheduler = CloudDriver.getInstance().getScheduler();

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

    public void initModule() throws Exception {

        Class<?> mainClass = classLoader.loadClass(moduleConfig.getMainClass());
        Constructor<?> constructor = mainClass.getDeclaredConstructor();
        Object instance = constructor.newInstance();
        if (!(instance instanceof DriverModule))
            throw new IllegalArgumentException("Main class (" + moduleConfig.getMainClass() + ") does not extend " + DriverModule.class.getName());

        module = (DriverModule) instance;
        module.setController(this);
        this.registerModuleTasks(module);

        classLoader.setModule(module);

    }

    @Override
    public void loadModule() {
        synchronized (this) {
            if (module == null) return; // was never initialized
            if (state != ModuleState.DISABLED) return; // must be disabled first

            this.logger.accept("INFO", "Module " + module + " is being loaded...");

            try {

                state = ModuleState.LOADED;
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                this.logger.accept("ERROR", "An error occurred while loading module " + module);
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

            this.logger.accept("INFO", "Module " + module + " is being enabled...");

            try {
                state = ModuleState.ENABLED;
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                this.logger.accept("ERROR", "An error occurred while enabling module " + module);
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

            this.logger.accept("INFO", "Module " + module + " is being disabled..");
            this.unregisterClassLoader.accept(classLoader);

            try {
                if (this.moduleConfig.getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
                    this.callTasks(this.state);
                }
            } catch (Throwable ex) {
                this.logger.accept("ERROR", "An error occurred while disabling module " + module);
                ex.printStackTrace();
            }

            state = ModuleState.DISABLED;
        }
    }

    @Override
    public void unregisterModule() {
        synchronized (this) {

            try {
                classLoader.close();
            } catch (Exception ex) {
                this.logger.accept("ERROR", "Unable to close classloader");
                ex.printStackTrace();
            }

            state = ModuleState.UNREGISTERED;
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
            config.set("enabled", defaultValue);
            config.save();
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
    public ModuleManager getManager() {
        return manager;
    }

    @Nonnull
    @Override
    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    @Nonnull
    @Override
    public DriverModule getModule() {
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
