package cloud.hytora.remote;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.commands.parameter.defaults.*;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.context.ApplicationContext;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.commands.sender.defaults.DefaultCommandSender;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.message.IChannelMessenger;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import cloud.hytora.driver.storage.RemoteNetworkDocumentStorage;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.uuid.IdentificationCache;
import cloud.hytora.remote.adapter.RemoteAdapter;
import cloud.hytora.remote.adapter.RemoteProxyAdapter;
import cloud.hytora.remote.impl.*;
import cloud.hytora.remote.impl.handler.*;
import cloud.hytora.remote.impl.log.DefaultLogHandler;
import cloud.hytora.remote.impl.module.RemoteModuleManager;
import lombok.Getter;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.jar.*;

@Getter
public class Remote extends CloudDriver<ICloudServer> {

    private static Remote instance;

    private final IApplicationContext applicationContext;
    private final CommandSender commandSender;
    private final Instrumentation instrumentation;

    private ClassLoader applicationClassLoader;
    private final ClassLoader bootClassLoader = getClass().getClassLoader();
    private final String[] arguments;

    @Setter
    private RemoteAdapter adapter;

    @Setter
    private Thread applicationThread;

    private final RemoteNetworkClient networkExecutor;
    private final RemoteIdentity property;


    public Remote(RemoteIdentity identity) {
        this(identity, new HandledAsyncLogger(identity.getLogLevel()).addHandler(new DefaultLogHandler()).addHandler(entry -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new DriverLogEvent(entry))), null, null);
    }

    public Remote(RemoteIdentity identity, Logger logger, Instrumentation instrumentation, String[] arguments) {
        super(logger, DriverEnvironment.SERVICE);

        instance = this;
        this.instrumentation = instrumentation;
        this.arguments = arguments;
        this.applicationContext = new ApplicationContext(this);
        this.applicationContext.setInstance("driver", CloudDriver.getInstance());


        this.commandSender = new DefaultCommandSender("Remote", null).function(System.out::println);
        this.property = identity;

        this.networkExecutor = new RemoteNetworkClient(property.getAuthKey(), property.getName(), property.getHostname(), property.getPort(), DocumentFactory.emptyDocument());

        //registering handlers
        this.networkExecutor.registerPacketHandler(new RemoteLoggingHandler());
        this.networkExecutor.registerPacketHandler(new RemoteCommandHandler());
        this.networkExecutor.registerPacketHandler(new RemoteCacheUpdateHandler());
        this.networkExecutor.registerPacketHandler(new RemoteNodeUpdateHandler());

        this.providerRegistry.setProvider(ICloudServiceTaskManager.class, new RemoteServiceTaskManager());
        this.providerRegistry.setProvider(ICloudServiceManager.class, new RemoteServiceManager());
        this.providerRegistry.setProvider(ICloudPlayerManager.class, new RemotePlayerManager());
        this.providerRegistry.setProvider(ICommandManager.class, new RemoteCommandManager());
        this.providerRegistry.setProvider(IChannelMessenger.class, new RemoteChannelMessenger());
        this.providerRegistry.setProvider(INodeManager.class, new RemoteNodeManager());
        this.providerRegistry.setProvider(IModuleManager.class, new RemoteModuleManager());
        this.providerRegistry.setProvider(INetworkDocumentStorage.class, new RemoteNetworkDocumentStorage(this.networkExecutor));
        this.providerRegistry.setProvider(IdentificationCache.class, new RemoteIdentificationCache());

        this.providerRegistry
                .getUnchecked(ICommandManager.class)
                .getParamTypeRegistry()
                .register(
                        new PlayerParamType(),
                        new TaskParamType(),
                        new ServiceParamType(),
                        new NodeParamType()
                );
        //service cycle update task
        Scheduler.runTimeScheduler().scheduleRepeatingTaskAsync(() -> {

            RemoteAdapter remoteAdapter = getAdapter();
            ICloudServer server = this.thisSidesClusterParticipant();

            if (remoteAdapter == null || server == null) {
                return;
            }
            server.setLastCycleData(remoteAdapter.createCycleData());
            server.update();
        }, SERVER_PUBLISH_INTERVAL, SERVER_PUBLISH_INTERVAL);

    }

    public synchronized void startApplication() throws Exception {

        String applicationFileName = this.arguments[0];
        logger.debug("Using '{}' as application file..", applicationFileName);

        Path applicationFile = Paths.get(applicationFileName);
        if (Files.notExists(applicationFile))
            throw new IllegalStateException("Application file " + applicationFileName + " does not exist");

        // create our own classloader and load all classes (only load don't initialize)
        // so the parent of the application's classloader is the system classloader, and not the platform classloader
        // but only for spigot servers >= 1.18, bungeecord plugin management will break with this logic, must be loaded with the system classloader directly
        // => "Plugin requires net.md_5.bungee.api.plugin.PluginClassloader"
        if (shouldPreloadClasses(applicationFile)) {
            applicationClassLoader = new URLClassLoader(new URL[]{applicationFile.toUri().toURL()}, ClassLoader.getSystemClassLoader());
            try (JarInputStream stream = new JarInputStream(Files.newInputStream(applicationFile))) {
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    // only resolve class files
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        // canonicalize the class name
                        String className = entry.getName().replace('/', '.').replace(".class", "");
                        // load the class
                        try {
                            Class.forName(className, false, applicationClassLoader);
                        } catch (Throwable ignored) {
                            // ignore
                        }
                    }
                }
            }
        } else {
            applicationClassLoader = ClassLoader.getSystemClassLoader();
        }

        // append application file to system class loader
        // could be problematic if the application (java9+) uses the platform or higher (-> bootstrap) classloader
        // dont append to bootstrap loader => classloader of application main class will magically be null
        // previous solution: https://github.com/anweisen/DyCloud/blob/a328842/wrapper/src/main/java/net/anweisen/cloud/wrapper/CloudWrapper.java#L190
        JarFile applicationJarFile = new JarFile(applicationFile.toFile());
        instrumentation.appendToSystemClassLoaderSearch(applicationJarFile);
        logger.info("Appended ApplicationJarFile to system classLoader search!");

        Attributes manifestAttributes = getManifestAttributes(applicationFile);

        String mainClassName = manifestAttributes.getValue("Main-Class");
        String premainClassName = manifestAttributes.getValue("Premain-Class");
        String agentClassName = manifestAttributes.getValue("Launcher-Agent-Class");
        logger.info("Found attributes main:{} premain:{} agent:{}", mainClassName, premainClassName, agentClassName);

        if (premainClassName != null) {
            try {
                Class<?> premainClass = Class.forName(premainClassName, true, applicationClassLoader);
                Method agentMethod = premainClass.getMethod("premain", String.class, Instrumentation.class);
                logger.info("Invoking premain method..");
                agentMethod.invoke(null, null, instrumentation);
                logger.info("Successfully invoked premain method");
            } catch (ClassNotFoundException ex) {
            } catch (Throwable ex) {
                logger.error("Unable to execute premain", ex);
            }
        }
        if (agentClassName != null) {
            try {
                Class<?> agentClass = Class.forName(agentClassName, true, applicationClassLoader);
                Method agentMethod = agentClass.getMethod("agentmain", String.class, Instrumentation.class);
                logger.info("Invoking agentmain method..");
                agentMethod.invoke(null, null, instrumentation);
                logger.info("Successfully invoked agentmain method");
            } catch (ClassNotFoundException ex) {
            } catch (Throwable ex) {
                logger.error("Unable to execute agentmain", ex);
            }
        }

        Class<?> mainClass = Class.forName(mainClassName, true, applicationClassLoader);
        Method mainMethod = mainClass.getMethod("main", String[].class);

        applicationThread = new Thread(() -> {
            try {
                logger.info("Starting application thread..");
                mainMethod.invoke(
                        null,
                        new Object[]{new String[0]}
                );
            } catch (Exception ex) {
                logger.error("Unable to start application..", ex);
                System.exit(0);
            }
        }, "Application-Thread");

        applicationThread.setContextClassLoader(applicationClassLoader);
        applicationThread.start();


    }


    @Nonnull
    private Attributes getManifestAttributes(@Nonnull Path applicationFile) {
        try (JarFile jarFile = new JarFile(applicationFile.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) throw new IllegalStateException("Manifest is null");
            return manifest.getMainAttributes();
        } catch (Exception ex) {
            throw new WrappedException("Unable to extract manifest attributes from jarfile", ex);
        }
    }

    // https://github.com/CloudNetService/CloudNet-v3/pull/560/files#diff-3e7f947c6535489177b7860ba2888ac02022f2427f48a6f4e9f12087f2951fbeR47-R55
    private boolean shouldPreloadClasses(@Nonnull Path applicationFile) {
        try (JarFile jarFile = new JarFile(applicationFile.toFile())) {
            return jarFile.getEntry("META-INF/versions.list") != null;
        } catch (Exception ex) {
            throw new WrappedException("Unable to find out whether to preload classes of jarfile", ex);
        }
    }


    public Task<DriverUpdatePacket> nexCacheUpdate() {
        Task<DriverUpdatePacket> task = Task.empty();
        task.denyNull();
        CloudDriver.getInstance().getNetworkExecutor().registerSelfDestructivePacketHandler((PacketHandler<DriverUpdatePacket>) (wrapper1, packet) -> task.setResult(packet));
        return task;
    }

    public static Remote getInstance() {
        return instance;
    }

    public RemoteProxyAdapter getProxyAdapter() {
        return perform(adapter instanceof RemoteProxyAdapter, () -> cast(adapter), new IllegalStateException("Not a " + RemoteProxyAdapter.class.getSimpleName()));
    }

    public RemoteProxyAdapter getProxyAdapterOrNull() {
        return perform(adapter instanceof RemoteProxyAdapter, () -> cast(adapter), (Supplier<RemoteProxyAdapter>) () -> null);
    }

    @Override
    public void shutdown() {
        if (adapter != null) {
            adapter.shutdown();
        }

        if (applicationThread != null) {
            //applicationThread.destroy();
        }
        System.exit(0);
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        DriverLoggingPacket packet = new DriverLoggingPacket(component, message);
        this.networkExecutor.sendPacket(packet);
    }

    @Override
    public ICloudServer thisSidesClusterParticipant() {
        return this.providerRegistry.get(ICloudServiceManager.class).mapOrElse(sm -> sm.getServiceByNameOrNull(this.property.getName()), () -> null);
    }


}