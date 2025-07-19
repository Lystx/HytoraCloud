package cloud.hytora.remote;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.common.task.TaskResult;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.sender.defaults.DefaultConsoleCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.common.message.base.def.DefaultChannelMessenger;
import cloud.hytora.driver.config.ConfigManager;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.event.listener.CloudEventListener;
import cloud.hytora.driver.event.defaults.remote.CloudEventRemoteConnectEvent;
import cloud.hytora.driver.common.message.base.ChannelMessenger;
import cloud.hytora.driver.language.LanguageManager;
import cloud.hytora.driver.language.def.DriverLanguageManager;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.auth.PacketAuthentication;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheRequest;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.def.CloudQuery;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceCycleData;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.ServiceTaskManager;
import cloud.hytora.driver.entity.services.utils.RemoteIdentity;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;

import cloud.hytora.remote.adapter.RemoteAdapter;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.impl.*;
import cloud.hytora.remote.impl.handler.*;
import cloud.hytora.remote.impl.log.DefaultLogHandler;
import cloud.hytora.remote.impl.module.RemoteModuleManager;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.*;

import static cloud.hytora.driver.networking.packets.response.NetworkResponseState.OK;

@Getter
public class Remote extends CloudDriver {

    private static Remote instance;
    private final ServiceTaskManager serviceTaskManager;
    private final ServiceManager serviceManager;

    private final PlayerManager playerManager;
    private final CommandManager commandManager;
    private final CommandSender commandSender;
    private final ChannelMessenger channelMessenger;
    private final ConfigManager configManager;
    private final NodeManager nodeManager;
    private final ModuleManager moduleManager;
    private final Instrumentation instrumentation;
    /**
     * The current {@link LanguageManager} instance
     *
     * @see LanguageManager
     */
    private LanguageManager languageManager;

    private ClassLoader applicationClassLoader;
    private final ClassLoader bootClassLoader = getClass().getClassLoader();
    private final String[] arguments;

    @Setter
    private RemoteAdapter adapter;

    @Setter
    private Thread applicationThread;

    private final RemoteNetworkClient client;
    private final RemoteIdentity property;


    public Remote(RemoteIdentity identity) {
        this(identity, new HandledAsyncLogger(identity.getLogLevel())
                .addHandler(new DefaultLogHandler()), null, null);


    }

    public Remote(RemoteIdentity identity, Logger logger, Instrumentation instrumentation, String[] arguments) {
        super(logger, Environment.SERVICE);

        instance = this;
        this.instrumentation = instrumentation;
        this.arguments = arguments;
        this.languageManager = new DriverLanguageManager("english");

        if (identity.getVersionType().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            DriverUtility.IS_BUNGEECORD = true;
        }

        this.commandSender = new DefaultConsoleCommandSender("Remote", null).function(System.out::println);
        this.property = identity;

        this.client = new RemoteNetworkClient(property.getAuthKey(), property.getName(), Document.empty());

        //registering handlers
        this.client.registerPacketHandler(new RemoteLoggingHandler());
        this.client.registerPacketHandler(new RemoteServiceHandler());
        this.client.registerPacketHandler(new RemoteCacheUpdateHandler());
        this.client.registerPacketHandler(new RemoteNodeUpdateHandler());

        this.client.registerPacketHandler(getTemplateManager());

        this.serviceTaskManager = new RemoteServiceTaskManager();
        this.serviceManager = new RemoteServiceManager();
        this.playerManager = new RemotePlayerManager(this.eventManager);
        this.commandManager = new RemoteCommandManager();
        this.channelMessenger = new DefaultChannelMessenger(this.client);
        this.nodeManager = new RemoteNodeManager();
        this.moduleManager = new RemoteModuleManager();
        this.configManager = new RemoteConfigManager();


        //registering command argument parsers
        this.commandManager.registerParser(ServiceVersion.class, ServiceVersion::valueOf);
        this.commandManager.registerParser(LogLevel.class, LogLevel::valueOf);
        this.commandManager.registerParser(CloudService.class, this.serviceManager::getCachedCloudService);
        this.commandManager.registerParser(ServiceTask.class, this.serviceTaskManager::getCachedServiceTask);
        this.commandManager.registerParser(CloudPlayer.class, this.playerManager::getCachedCloudPlayer);
        this.commandManager.registerParser(CloudOfflinePlayer.class, s -> this.playerManager.getOfflinePlayer(s).timeOut(TimeUnit.SECONDS, 5).syncUninterruptedly().get());
        this.commandManager.registerParser(INode.class, this.nodeManager::getCachedNode);

        this.setProvider(Query.class, new CloudQuery());

        System.out.println(" ");
        System.out.println(" ");
        System.out.println("-----------------------------------------------");
        TaskResult<Channel> channel = this.client.openConnection(property.getHostname(), property.getPort()).syncUninterruptedly().get();

        DriverUtility.printColored("Remote", "Bound NetworkClient to %2" + channel.getResult());

        PacketAuthentication authenticationPacket = new PacketAuthentication(PacketAuthentication.AuthenticationPayload.SERVICE, Remote.getInstance().getProperty());

        BufferedResponse response = authenticationPacket.sendQuery().execute().syncUninterruptedly().get();
        NetworkResponseState state = response.state();
        DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §7" + response.state());

        if (state == OK) {
            Remote.getInstance().getScheduledExecutor().scheduleAtFixedRate(() -> {
                Remote.getInstance().publishCycleData();
            }, 0, Constants.SERVER_PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);
        }
        switch (state) {
            case OK:

                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventRemoteConnectEvent());
                DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §aCOMPLETED");

                INetworkConfig networkConfig = this.configManager.readConfig();//connected now read config
                DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §aCONFIG_RECEIVE");
                DriverUtility.printColored("Remote", "Overview of §eimportant Settings§8:");
                DriverUtility.printColored("Remote", "    => %1NetworkId§8: %2" + networkConfig.getUniqueNetworkId());
                DriverUtility.printColored("Remote", "    => %1Logging§8: %2" + networkConfig.getLogLevel());
                DriverUtility.printColored("Remote", "    => %1ServiceProcessing§8: %2" + networkConfig.getServiceProcessType());
                DriverUtility.printColored("Remote", "    => %11st Color§8: %2" + networkConfig.getMessages().getMainColor().name());
                DriverUtility.printColored("Remote", "    => %12nd Color§8: %2" + networkConfig.getMessages().getSecondColor().name());
                System.out.println(" ");
                DriverUtility.printColored("Remote", "If something here §cdoes not §7appear right, please contact a %1HytoraCloud§8-%2Developer§8!");

                System.out.println(" ");
                System.out.println(" ");
                System.out.println("-----------------------------------------------");
                System.out.println(" ");
                System.out.println(" ");
                break;

            case FAILED:
            case ERROR:
            case BAD_REQUEST:
                String errorMessage = response.buffer().readString();
                DriverUtility.printColored("Remote", "§cSomething went wrong whilst authenticating this service to the provided Node!");
                DriverUtility.printColored("Remote", "§cError: §e" + errorMessage);
                System.out.println(" ");
                System.out.println(" ");
                System.out.println("-----------------------------------------------");
                System.out.println(" ");
                System.out.println(" ");
                break;
        }

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


    public Task<PacketDriverCacheUpdate> nexCacheUpdate() {
        Task<PacketDriverCacheUpdate> task = Task.empty();
        task.denyNull();
        CloudDriver.getInstance().getExecutor().registerSelfDestructivePacketHandler((PacketHandler<PacketDriverCacheUpdate>) (wrapper1, packet) -> task.setResult(packet));
        return task;
    }

    public static Remote getInstance() {
        return instance;
    }

    public RemoteProxyAdapter getProxyAdapter() {
        return DriverUtility.perform(adapter instanceof RemoteProxyAdapter, () -> DriverUtility.cast(adapter), new IllegalStateException("Not a " + RemoteProxyAdapter.class.getSimpleName() + " but a " + adapter.getClass().getName()));
    }

    public RemoteProxyAdapter getProxyAdapterOrNull() {
        return DriverUtility.perform(adapter instanceof RemoteProxyAdapter, () -> DriverUtility.cast(adapter), (Supplier<RemoteProxyAdapter>) () -> null);
    }

    public CloudService thisService() {
        return this.serviceManager == null ? null : this.serviceManager.getAllCachedServices().stream().filter(it -> it.getName().equalsIgnoreCase(this.property.getName())).findAny().orElse(null);
    }


    @Override
    public void shutdown() {
        if (adapter != null) {
            adapter.shutdown().onTaskSucess(e -> {

                if (applicationThread != null) {
                    //applicationThread.destroy();
                }
                System.exit(0);
            });
            return;
        } else {
            System.exit(0);
        }

    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        PacketDriverLogging packet = new PacketDriverLogging(component, message);
        this.client.sendPacket(packet);
    }

    public void publishCycleData() {
        CloudService server = Remote.getInstance().thisService();
        ServiceCycleData cycleData = createCycleData();
        if (cycleData == null) {
            return;
        }

        server.setLastCycleData(cycleData);
        server.update(PublishingType.GLOBAL);
    }

    public ServiceCycleData createCycleData() {

        RemoteAdapter remoteAdapter = Remote.getInstance().getAdapter();
        CloudService server = Remote.getInstance().thisService();

        if (remoteAdapter == null || server == null) {
            return null;
        }
        return remoteAdapter.createCycleData();
    }

    @NotNull
    @Override
    public HandlingNetworkExecutor getExecutor() {
        return client;
    }


    @SafeVarargs
    public static Remote init(RemoteIdentity identity, Consumer<PacketDriverCacheUpdate>... onInit) {
        if (instance == null) { //processing is PLUGIN_BRIDGE
            Remote remote = new Remote(identity);
            PacketDriverCacheRequest packet = new PacketDriverCacheRequest();
            BufferedResponse bufferedResponse = packet.sendQuery().execute().syncUninterruptedly().get();
            try {
                IPacket iPacket = bufferedResponse.buffer().readPacket();
                for (Consumer<PacketDriverCacheUpdate> driverUpdatePacketConsumer : onInit) {
                    driverUpdatePacketConsumer.accept((PacketDriverCacheUpdate) iPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return remote;
        } else {
            for (Consumer<PacketDriverCacheUpdate> driverUpdatePacketConsumer : onInit) {
                driverUpdatePacketConsumer.accept(null);
            }
            return instance;
        }
    }


    public static void initFromOtherInstance(RemoteIdentity identity, Consumer<AbstractPacket> handler, Runnable
            end) {

        Remote remote = new Remote(identity);

        remote.getEventManager().registerListener(new CloudEventListener<>(CloudEventRemoteConnectEvent.class, e -> {
            handler.accept(null);
        }));
    }
}
