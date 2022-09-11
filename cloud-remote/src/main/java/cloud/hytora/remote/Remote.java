package cloud.hytora.remote;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.IPromise;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.parameter.defaults.NodeParamType;
import cloud.hytora.driver.commands.parameter.defaults.PlayerParamType;
import cloud.hytora.driver.commands.parameter.defaults.ServiceParamType;
import cloud.hytora.driver.commands.parameter.defaults.TaskParamType;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.commands.sender.defaults.DefaultCommandSender;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.message.IChannelMessenger;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverLoggingPacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.GenericQueryPacket;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import cloud.hytora.driver.storage.RemoteNetworkDocumentStorage;
import cloud.hytora.driver.sync.ISyncedNetworkPromise;
import cloud.hytora.driver.sync.SyncedObjectType;
import cloud.hytora.driver.uuid.IdentificationCache;
import cloud.hytora.remote.impl.*;
import cloud.hytora.remote.impl.handler.RemoteLoggingHandler;
import cloud.hytora.remote.impl.handler.RemoteNodeUpdateHandler;
import cloud.hytora.remote.impl.log.DefaultLogHandler;
import cloud.hytora.remote.impl.module.RemoteModuleManager;
import lombok.Getter;
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
import java.util.jar.*;

/**
 * The {@link Remote} is the Service-Side implementation of the {@link CloudDriver}
 * The Remote also holds a {@link RemoteIdentity} and the {@link IClusterObject} on this
 * side is the current {@link ICloudServer} this remote is running for
 * The main difference between other driver-implementations is that the {@link Remote}
 * is capable or is even only used to start applications with custom class-loaders etc
 *
 * @author Lystx
 * @see CloudDriver
 * @since SNAPSHOT-1.1
 */
@Getter
public class Remote extends CloudDriver<ICloudServer> {

    /**
     * The static instance to override CloudDriver instance
     */
    @Getter
    private static Remote instance;

    /**
     * The current driver command sender instance
     */
    private final CommandSender commandSender;

    /**
     * The java instrumentation for class loading
     */
    private final Instrumentation instrumentation;

    /**
     * The class loader that was used on startup
     */
    private final ClassLoader bootClassLoader = getClass().getClassLoader();

    /**
     * The provided start-arguments
     */
    private final String[] arguments;

    /**
     * The network client for remote side
     */
    private final RemoteNetworkClient networkExecutor;

    /**
     * The identity that belongs to this remote
     */
    private final RemoteIdentity property;

    /**
     * The application thread the application runs in
     */
    @Setter
    private Thread applicationThread;

    /**
     * The current class loader for the starting application
     */
    private ClassLoader applicationClassLoader;

    /**
     * Constructs a new {@link Remote} instance by just a {@link RemoteIdentity}
     * The logger is auto-constructed and no arguments or instrumentation are defined
     *
     * @param identity the identity to use
     * @since SNAPSHOT-1.3
     */
    public Remote(RemoteIdentity identity) {
        this(identity, new HandledAsyncLogger(identity.getLogLevel()).addHandler(new DefaultLogHandler()).addHandler(entry -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new DriverLogEvent(entry))), null, null);
    }

    public Remote(RemoteIdentity identity, Logger logger, Instrumentation instrumentation, String[] arguments) {
        super(logger, DriverEnvironment.SERVICE);

        instance = this;
        this.instrumentation = instrumentation;
        this.arguments = arguments;
        this.property = identity;

        this.commandSender = new DefaultCommandSender("Remote", null).function(System.out::println);

        this.networkExecutor = new RemoteNetworkClient(property.getAuthKey(), property.getName(), property.getHostname(), property.getPort(), DocumentFactory.emptyDocument());

        //registering handlers
        this.networkExecutor.registerPacketHandler(new RemoteLoggingHandler());
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
    }

    /**
     * Starts the application by using the provied start-arguments
     * to get the application-main-class, the fileName etc.
     * <p>
     * Application is always started in a different thread with a
     * different {@link ClassLoader}
     *
     * @throws Exception if something goes wrong
     */
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

        Attributes manifestAttributes;
        try (JarFile jarFile = new JarFile(applicationFile.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) throw new IllegalStateException("Manifest is null");
            manifestAttributes = manifest.getMainAttributes();
        } catch (Exception ex) {
            throw new WrappedException("Unable to extract manifest attributes from jarfile", ex);
        }

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

    /**
     * Returns a task that awaits the next {@link DriverUpdatePacket}
     * to check when the whole cache of this instance has been updated
     */
    public IPromise<DriverUpdatePacket> nexCacheUpdate() {
        IPromise<DriverUpdatePacket> task = IPromise.empty();
        CloudDriver
                .getInstance()
                .getNetworkExecutor()
                .registerSelfDestructivePacketHandler(
                        (PacketHandler<DriverUpdatePacket>)
                                (wrapper1, packet) -> task.setResult(packet)
                );
        return task;
    }

    @Override
    public void shutdown() {

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
    public @NotNull ICloudServer thisSidesClusterParticipant() {
        return this.providerRegistry.get(ICloudServiceManager.class).mapOrElse(sm -> sm.getService(this.property.getName()), () -> null);
    }

    @Override
    public void updateThisSidesClusterParticipant() {
        this.thisSidesClusterParticipant().update();
    }

    @Override
    public <E extends IBufferObject> ISyncedNetworkPromise<E> getSyncedNetworkObject(SyncedObjectType<E> type, String queryParameters) {
        RemoteSyncedNetworkPromise<E> promise = new RemoteSyncedNetworkPromise<>();
        GenericQueryPacket<E> packet = new GenericQueryPacket<E>
                (
                        "cloud_internal_sync",
                        Document.newJsonDocument
                                (
                                        "id", type.getId(),
                                        "parameter", queryParameters
                                )
                ).query()
                .syncUninterruptedly()
                .get();

        //getting values from result-packet
        E result = packet.getResult();
        Throwable error = packet.getError();

        //setting values to promise
        promise.setObject(result);
        promise.setError(error);

        return promise;
    }

    @Override
    public @NotNull <E extends IBufferObject> IPromise<ISyncedNetworkPromise<E>> getSyncedNetworkObjectAsync(SyncedObjectType<E> type, String queryParameters) {
        IPromise<ISyncedNetworkPromise<E>> task = IPromise.empty();
        new GenericQueryPacket<E>
                (
                        "cloud_internal_sync",
                        Document.newJsonDocument
                                (
                                        "id", type.getId(),
                                        "parameter", queryParameters
                                )
                ).query()
                .onTaskSucess(packet -> {
                    RemoteSyncedNetworkPromise<E> promise = new RemoteSyncedNetworkPromise<>();

                    //getting values from result-packet
                    E result = packet.getResult();
                    Throwable error = packet.getError();

                    //setting values to promise
                    promise.setObject(result);
                    promise.setError(error);

                    task.setResult(promise);
                });
        return task;
    }

    /**
     * Checks if an application file needs to pre-load all classes of the file
     * depending on an existing versions.list inside the META-INF
     *
     * @param applicationFile the file to check
     * @return if should pre-load classes
     */
    private boolean shouldPreloadClasses(@Nonnull Path applicationFile) {
        try (JarFile jarFile = new JarFile(applicationFile.toFile())) {
            return jarFile.getEntry("META-INF/versions.list") != null;
        } catch (Exception ex) {
            throw new WrappedException("Unable to find out whether to preload classes of jarfile", ex);
        }
    }

}