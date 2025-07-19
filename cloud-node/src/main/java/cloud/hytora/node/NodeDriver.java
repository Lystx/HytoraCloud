package cloud.hytora.node;

import cloud.hytora.Expiration;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.common.task.TaskResult;
import cloud.hytora.common.task.exception.TaskTimedOutException;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.sender.defaults.DefaultConsoleCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;


import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.message.base.def.DefaultChannelMessenger;
import cloud.hytora.driver.config.*;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.driver.common.http.api.HttpServer;
import cloud.hytora.driver.common.http.impl.NettyHttpServer;
import cloud.hytora.driver.common.message.base.ChannelMessenger;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.config.def.UniversalNetworkConfig;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverAlmostOutOfMemory;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.language.LanguageManager;
import cloud.hytora.driver.language.Translation;
import cloud.hytora.driver.language.def.DriverLanguageManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheRequest;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.def.CloudQuery;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.services.fallback.SimpleFallback;
import cloud.hytora.driver.entity.services.template.def.CloudTemplate;
import cloud.hytora.driver.entity.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;

import cloud.hytora.node.console.NodeScreenManager;
import cloud.hytora.node.impl.Log4JAppender;
import cloud.hytora.node.impl.handler.packet.*;
import cloud.hytora.node.impl.module.NodeModuleManager;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.PacketRegistry;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.entity.services.task.ServiceTaskManager;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.UniversalServiceTask;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import cloud.hytora.driver.database.LocalStorage;
import cloud.hytora.node.impl.node.BaseNode;
import cloud.hytora.node.impl.setup.NetworkSetup;
import cloud.hytora.node.impl.setup.NodeRemoteSetup;
import cloud.hytora.node.remote.*;
import cloud.hytora.node.remote.handler.NodeRemoteCacheHandler;
import cloud.hytora.node.remote.handler.NodeRemoteHandler;
import cloud.hytora.node.remote.handler.NodeRemoteLoggingHandler;
import cloud.hytora.node.remote.handler.NodeRemoteServerHandler;
import cloud.hytora.node.remote.player.NodeRemotePlayerManager;
import cloud.hytora.node.service.InternalNotifyListener;
import cloud.hytora.node.service.template.LocalTemplateStorage;
import cloud.hytora.driver.common.setup.SetupControlState;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.node.impl.command.*;
import cloud.hytora.node.impl.command.impl.*;
import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.node.impl.node.NodeNodeManager;
import cloud.hytora.node.impl.setup.database.MongoDBSetup;
import cloud.hytora.node.impl.setup.database.MySqlSetup;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.impl.setup.NodeSetup;
import cloud.hytora.node.impl.config.NodeConfigManager;
import cloud.hytora.driver.command.console.Console;
import cloud.hytora.driver.config.def.UniversalDatabaseConfig;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.node.impl.database.cloud.DefaultDatabaseManager;
import cloud.hytora.node.service.NodeServiceTaskManager;
import cloud.hytora.node.impl.node.NodeBasedClusterExecutor;
import cloud.hytora.node.impl.player.NodePlayerManager;
import cloud.hytora.node.service.helper.NodeServiceQueue;


import cloud.hytora.remote.impl.RemoteServiceTaskManager;
import com.sun.management.OperatingSystemMXBean;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import javax.management.NotificationEmitter;
import java.io.File;
import java.io.IOException;
import java.lang.management.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cloud.hytora.common.logging.LogLevel.INFO;

@Getter
@Setter
public class NodeDriver extends CloudDriver {

    @Getter
    private static NodeDriver instance;

    private NodeConfigManager configManager;
    private Console console;
    private CommandManager commandManager;
    private CommandSender commandSender;


    private INode node;

    private IDatabaseManager databaseManager;
    private ServiceTaskManager serviceTaskManager;
    private ServiceManager serviceManager;
    private PlayerManager playerManager;
    private ModuleManager moduleManager;
    private ChannelMessenger channelMessenger;
    private NodeManager nodeManager;
    private HttpServer webServer;

    /**
     * The current {@link LanguageManager} instance
     *
     * @see LanguageManager
     */
    private LanguageManager languageManager;
    private Cluster executor;
    private ServiceQueue serviceQueue;

    public NodeDriver(Logger logger, Console console, String modulePath) throws Exception {
        super(logger, Environment.NODE);
        instance = this;

        this.running = true;
        this.console = console;


        Logger.setMessageFormater(s -> {
            if (CloudDriver.getInstance() != null) {
                ConfigManager configManager = CloudDriver.getInstance().getConfigManager();
                if (configManager != null) {
                    if (configManager.getConfig() != null) {
                        UniversalCloudMessages messages = configManager.getConfig().getMessages();
                        s = s.replaceAll("%1", messages.getMainColor().getColor());
                        s = s.replaceAll("%2", messages.getSecondColor().getColor());
                    }
                }
            }
            return s;
        });

        CloudDriver.Constants.MODULE_FOLDER = new File(modulePath);

        //setting node screen manager
        this.setProvider(ScreenManager.class, new NodeScreenManager());

        ScreenManager screenManager = this.getProvider(ScreenManager.class);
        Screen consoleScreen = screenManager
                .registerScreen("console", true)
                .registerTabCompleter(buffer -> {

                    if (buffer == null) {
                        return new ArrayList<>();
                    }

                    return CloudDriver.getInstance()
                            .getCommandManager()
                            .completeCommand(CloudDriver.getInstance().getCommandSender(), buffer);
                });

        //joining console screen
        screenManager.joinScreen(consoleScreen);

        //default to english -> will be changed on node instance when config is loaded
        // or on remote side when booting up server
        this.languageManager = new DriverLanguageManager("english");

        logger.info(Translation.of("global.application.context.load.start"));

        logger.info(Translation.of("global.application.context.load.success"));
        consoleScreen.clear();

        this.logger.setMinLevel(INFO);

        //loading console
        this.console.addInputHandler(s -> {
            CloudDriver.getInstance().getCommandManager().executeCommand(CloudDriver.getInstance().getCommandSender(), s);
        });

        this.commandSender = new DefaultConsoleCommandSender("Main", this.console).forceFunction((ExceptionallyConsumer<String>) s -> console.forceWrite(ColoredMessageFormatter.format(new LogEntry(Instant.now(), "node", s, INFO, null))));
        this.commandManager = new NodeCommandManager();

        //checking if setup required
        if (!CloudDriver.Constants.CONFIG_FILE.exists()) {


            new NetworkSetup().start(((networkSetup, setupControlState) -> {
                if (setupControlState != SetupControlState.FINISHED) {
                    return;
                }

                String nodeName = networkSetup.getName().trim();
                DatabaseType databaseType = networkSetup.getDatabaseType();
                long memory = networkSetup.getMemory();
                switch (networkSetup.getNodeType()) {
                    case STANDALONE:

                        //loading config
                        IConfig config = loadConfig(false);
                        new NodeSetup(nodeName, false, databaseType, memory).start((setup, sct) -> {
                            this.initNodeConfig(setup);
                        });
                        break;
                    case SLAVE:

                        //loading config
                        IConfig remoteConfig = loadConfig(true);
                        new NodeRemoteSetup(nodeName, true, databaseType, memory).start((setup1, state) -> {
                            initRemoteConfig(setup1);
                        });
                        break;
                }
            }));

            return;
        } else {
            this.logger.trace("Setup already done ==> Skipping...");
        }


        //loading config
        IConfig config = loadConfig();
        this.commandManager.setActive(true);

        //avoid log4j errors
        org.apache.log4j.BasicConfigurator.configure(new Log4JAppender());

        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("%1    __  __      __                   ________                __");
        this.logger.info("%1   / / / /_  __/ /_____  _________ _/ ____/ /___  __  ______/ /");
        this.logger.info("%1  / /_/ / / / / __/ __ \\/ ___/ __ `/ /   / / __ \\/ / / / __  / ");
        this.logger.info("%1 / __  / /_/ / /_/ /_/ / /  / /_/ / /___/ / /_/ / /_/ / /_/ /  ");
        this.logger.info("%1/_/ /_/\\__, _____\\____/_/   \\__,_/\\____________/\\________,_/   ");
        this.logger.info("%1      /____/ ___/____ ___  __  _______/ __/  | |  / <  /       ");
        this.logger.info("%1 ______    \\__ \\/ __ `__ \\/ / / / ___/ /_    | | / // /  ______");
        this.logger.info("%1/_____/   ___/ / / / / / / /_/ / /  / __/    | |/ // /  /_____/");
        this.logger.info("%1         /____/_/ /_/ /_/\\__,_/_/  /_/       |___//_/          ");
        this.logger.info("§8");
        this.logger.info(Translation.of("node.startup.header.version", VersionInfo.getCurrentVersion()));
        this.logger.info(Translation.of("node.startup.header.developer", "Lystx"));
        this.logger.info(Translation.of("node.startup.header.classloader", Thread.currentThread().getContextClassLoader()));
        this.logger.info("§8==================================================");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8");
        if (UniversalNetworkConfig.getInstance().getJavaVersions().isEmpty()) {
            this.logger.info("%1JavaVersions§8: %2" + System.getProperty("java.version"));

        } else {
            this.logger.info("%1JavaVersions§8: %2" + System.getProperty("java.version") + "§8, %2" + UniversalNetworkConfig.getInstance().getJavaVersions().stream().map(IJavaVersion::getName).collect(Collectors.joining("§8, %1")));
        }
        this.logger.info(Translation.of("node.startup.message.boot"));
        this.node = new BaseNode(configManager);


        this.databaseManager = new DefaultDatabaseManager(configManager.universal().getDatabaseConfig().getType(), configManager.universal().getDatabaseConfig());
        this.setProvider(IDatabaseManager.class, this.databaseManager);

        if (this.configManager.universal().getNodeConfig().isRemote()) {
            INodeConfig node = config.getNodeConfig();
            this.executor = new RemoteClusterExecutor(node.getAuthKey(), node.getNodeName(), Document.gson());
        } else {
            this.executor = new NodeBasedClusterExecutor(this.configManager.getConfig());

            LocalStorage database = this.databaseManager.getLocalStorage();
            database.registerSection("tasks", UniversalServiceTask.class);
            database.registerSection("groups", DefaultTaskGroup.class);
        }

        this.executor.registerPacketHandler(getTemplateManager());
        this.eventManager.registerListener(this);
        this.eventManager.registerListener(new InternalNotifyListener());

        this.serviceTaskManager = DriverUtility.get(this.configManager.isHeadNode(), NodeServiceTaskManager::new, RemoteServiceTaskManager::new);
        this.serviceManager = DriverUtility.get(this.configManager.isHeadNode(), NodeServiceManager::new, NodeRemoteServiceManager::new);
        this.playerManager = DriverUtility.get(this.configManager.isHeadNode(), () -> new NodePlayerManager(this.getEventManager()), () -> new NodeRemotePlayerManager(getEventManager()));
        this.channelMessenger = new DefaultChannelMessenger(executor);
        this.nodeManager = new NodeNodeManager(this.node);
        this.moduleManager = DriverUtility.get(this.configManager.isHeadNode(), NodeModuleManager::new, NodeRemoteModuleManager::new);
        this.logger.info("§8");

        //creating needed files
        this.logger.trace(Translation.of("node.startup.message.folder.create"));
        CloudDriver.Constants.NODE_FOLDER.mkdirs();

        CloudDriver.Constants.STORAGE_FOLDER.mkdirs();
        CloudDriver.Constants.STORAGE_VERSIONS_FOLDER.mkdirs();

        CloudDriver.Constants.SERVICE_DIR.mkdirs();
        CloudDriver.Constants.SERVICE_DIR_STATIC.mkdirs();
        CloudDriver.Constants.SERVICE_DIR_DYNAMIC.mkdirs();

        if (this.node.getConfig().isRemote()) {
            FileUtils.delete(CloudDriver.Constants.MODULE_FOLDER.toPath());


            this.logger.info(Translation.of("remote.startup.message.slave"));
            Channel channel = this.connectToHeadNode(config).syncUninterruptedly().get();
            this.logger.info(Translation.of("remote.startup.message.slave.success", channel));
        } else {
            this.logger.info(Translation.of("node.startup.message.headnode"));
            CloudDriver.Constants.DATABASE_FOLDER.mkdirs();
        }


        //checking if directories got deleted meanwhile
        for (TaskGroup parent : this.serviceTaskManager.getAllCachedTaskGroups()) {

            //creating templates
            for (ServiceTemplate template : parent.getTemplates()) {
                TemplateStorage storage = template.getStorage();
                if (storage != null) {
                    storage.createTemplate(template);
                }
            }
        }

        FileUtils.setTempDirectory(Paths.get(".temp"));

        //registering template storage
        if (this.configManager.isHeadNode()) {
            this.templateManager.registerStorage(new LocalTemplateStorage());
        } else {
            this.templateManager.registerStorage(new RemoteTemplateStorage());
        }
        //copying files
        this.logger.trace(Translation.of("node.startup.message.files.copy"));

        this.logger.trace(Translation.of("node.startup.message.command.register"));
        this.commandManager.registerCommand(new ShutdownCommand());
        this.commandManager.registerCommand(new HelpCommand());
        this.commandManager.registerCommand(new NodeCommand());
        this.commandManager.registerCommand(new TaskCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new ServiceCommand());
        this.commandManager.registerCommand(new ModuleCommand());
        this.commandManager.registerCommand(new DebugCommand());
        this.commandManager.registerCommand(new PlayerCommand());
        this.commandManager.registerCommand(new TickCommand());
        this.commandManager.registerCommand(new ClusterCommand());
        this.commandManager.registerCommand(new LoggerCommand());

        //registering command argument parsers
        this.commandManager.registerParser(ServiceVersion.class, ServiceVersion::valueOf);
        this.commandManager.registerParser(LogLevel.class, LogLevel::valueOf);
        this.commandManager.registerParser(CloudService.class, this.serviceManager::getCachedCloudService);
        this.commandManager.registerParser(ModuleController.class, this.moduleManager::getModule);
        this.commandManager.registerParser(ServiceTask.class, this.serviceTaskManager::getCachedServiceTask);
        this.commandManager.registerParser(CloudPlayer.class, this.playerManager::getCachedCloudPlayer);
        this.commandManager.registerParser(CloudOfflinePlayer.class, s -> this.playerManager.getOfflinePlayer(s).timeOut(TimeUnit.SECONDS, 5).syncUninterruptedly().get());
        this.commandManager.registerParser(INode.class, this.nodeManager::getCachedNode);

        //registering packet handlers
        this.logger.trace(Translation.of("node.startup.message.packet.register"));

        //remote packet handlers
        this.executor.registerPacketHandler(new NodeRemoteServerHandler());
        this.executor.registerPacketHandler(new NodeServiceHandler());

        if (!this.configManager.isRemote()) {
            this.executor.registerPacketHandler(new NodeRedirectPacketHandler());
            this.executor.registerPacketHandler(new AuthenticationHandler());
            this.executor.registerPacketHandler(new NodeCacheRequestHandler());
            this.executor.registerPacketHandler(new NodeDataCycleHandler());
            this.executor.registerPacketHandler(new NodeOfflinePlayerPacketHandler());
            this.executor.registerPacketHandler(new NodeModulePacketHandler());
            this.executor.registerPacketHandler(new NodeModuleControllerPacketHandler());
            this.executor.registerPacketHandler(new NodeLoggingPacketHandler());
        } else {
            this.executor.registerPacketHandler(new NodeRemoteLoggingHandler());
            this.executor.registerPacketHandler(new NodeRemoteCacheHandler());
            this.executor.registerPacketHandler(new NodeRemoteHandler());
        }


        this.logger.trace(Translation.of("node.startup.message.packet.register.end", PacketRegistry.getRegisteredPackets().size()));
        this.logger.trace("§8");

        if (this.configManager.isHeadNode()) {
            //heart-beat execution for time out checking
            TimeOutChecker check = new TimeOutChecker();
            scheduledExecutor.scheduleAtFixedRate(check, 1, 1, TimeUnit.SECONDS);

            //starting web-server
            this.webServer = new NettyHttpServer();
            this.setProvider(HttpServer.class, this.webServer);
            for (ProtocolAddress address : configManager.getConfig().getHttpListeners()) {
                this.webServer.addListener(address);
            }

        }

        //Query provider
        this.setProvider(Query.class, new CloudQuery());

        this.commandManager.setActive(true);

        //managing and loading modules
        this.moduleManager.setModulesDirectory(CloudDriver.Constants.MODULE_FOLDER.toPath());
        this.moduleManager.resolveModules();
        if (this.configManager.isHeadNode()) {
            this.moduleManager.loadModules(); //remote invokes the method automatically after resolving

            //enabling modules after having loaded the database
            this.moduleManager.enableModules();
        }

        this.logger.info(Translation.of("node.startup.message.modules.loaded"), moduleManager.getModules().size(), moduleManager.getModules().stream().map(m -> m.getModuleConfig().getName()).collect(Collectors.toList()));
        this.logger.info("§7To learn more about a specific module type %1module info <name>");


        // print finish successfully message
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info(Translation.of("node.startup.message.end.line.one"));
        this.logger.info(Translation.of("node.startup.message.end.line.two"));
        this.logger.info("§8");
        this.logger.info("§8");

        if (this.configManager.isRemote()) {
            this.logger.info(Translation.of("remote.client.demand.driver.cache"));
            PacketDriverCacheUpdate cacheUpdate = new PacketDriverCacheRequest()
                    .sendQuery()
                    .execute()
                    .timeOut(TimeUnit.SECONDS, 5)
                    .throwOnTimeOut(TaskTimedOutException::new)
                    .syncUninterruptedly()
                    .get()
                    .buffer()
                    .readPacket(PacketDriverCacheUpdate.class);
            this.logger.info(Translation.of("remote.client.received.driver.cache"));
            this.logger.trace("   §8» %2Nodes§8: %1{} §8| %2Groups§8: %1{} §8| %2Tasks§8: %1{} §8| %2Services§8: %1{} §8| %2Players§8: %1{}", cacheUpdate.getAllCachedNodes().size(), cacheUpdate.getAllCachedTaskGroups().size(), cacheUpdate.getAllCachedServiceTasks().size(), cacheUpdate.getAllCachedServices().size(), cacheUpdate.getAllCachedCloudPlayers().size());

            this.logger.info(Translation.of("remote.client.demand.driver.files"));

            PacketChannel channel = executor.getPacketChannel();

            Map<Path, String> filePaths = channel.sendPacketQuery(PacketCloudEntityNode.forFilesRequest()).buffer().readMap(buffer -> Paths.get(buffer.readString()), PacketBuffer::readString);
            int amount = filePaths.keySet().size();
            AtomicInteger i = new AtomicInteger(0);
            this.logger.info(Translation.of("remote.client.transfer.driver.files"), amount);
            filePaths.forEach((path, fileName) -> {
                PacketCloudEntityNode packetCloudEntityNode = PacketCloudEntityNode.forFileRequest(path.toString());
                logger.trace("File {} | QueryId: [{}]", path, packetCloudEntityNode.transferInfo().getInternalQueryId());


                PacketBuffer buffer = channel.sendPacketQuery(packetCloudEntityNode).buffer();
                Path resolved = Constants.STORAGE_VERSIONS_FOLDER.toPath().resolve(fileName);
                buffer.readFile(resolved.toFile());

                if (i.incrementAndGet() >= amount) {
                    receivedFiles = true;
                    this.logger.info(Translation.of("remote.client.received.driver.files"));
                }
            });
        }

        //starting service queue
        this.serviceQueue = DriverUtility.get(this.configManager.isHeadNode(), NodeServiceQueue::new, RemoteServiceQueue::new);
        //add node cycle data
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (CloudDriver.getInstance().getNodeManager().isHeadNode()) {
                executor.sendPacketToAll(PacketCloudEntityNode.forCycleData(this.node, this.node.getLastCycleData()));
            } else {
                executor.sendPacket(PacketCloudEntityNode.forCycleData(this.node, this.node.getLastCycleData()));
            }
        }, 1_000, Constants.NODE_PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);

        MemoryPoolMXBean tenuredGen = ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .filter(MemoryPoolMXBean::isUsageThresholdSupported)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Can't find tenured generation MemoryPoolMXBean"));
        double threshold = 0.99;
        MemoryUsage usage = tenuredGen.getUsage();
        tenuredGen.setCollectionUsageThreshold((int) Math.floor(usage.getMax()
                * threshold));
        tenuredGen.setUsageThreshold((int) Math.floor(usage.getMax() * threshold));

        NotificationEmitter notificationEmitter =
                (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        notificationEmitter.addNotificationListener((notification, handback) -> {
            if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
                com.sun.management.OperatingSystemMXBean system = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                float cpuUsage = (float) system.getSystemCpuLoad() * 100f;
                long freeRam = system.getFreePhysicalMemorySize() / 1024 / 1024; // bytes -> kilobytes -> megabytes

                this.eventManager.callEvent(new CloudEventDriverAlmostOutOfMemory(this.node.getName(), cpuUsage, freeRam), PublishingType.GLOBAL);
            }
        }, null, null);

        // add a shutdown hook for fast closes
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

    }


    @EventListener
    public void handleMemoryLow(CloudEventDriverAlmostOutOfMemory event) {


        if (!Expiration.getInstance().hasExpired("outOfMemory")) {
            return;
        }

        String driverName = event.getDriverName();
        if (driverName.equalsIgnoreCase(this.getNode().getName())) {
            //this node is running low on memory
            for (String line : Translation.listOf("node.lowmemory.warn.self", event.getCpuUsage(), event.getFreeMemory())) {
                logger.warn(line);
            }
            CloudDriver.getInstance().setSingleThread(true);
            return;
        }
        //if other process and this is headNode... still warning
        logger.warn(Translation.of("node.lowmemory.warn.other"), driverName);
        Expiration.getInstance().wait("outOfMemory");
    }


    /**
     * Only used on Node-remote
     * It indicates if the remote has received all files from headNode
     */
    private boolean receivedFiles = false;

    private Task<Channel> connectToHeadNode(IConfig config) {
        Task<Channel> connectTask = Task.empty();
        INodeConfig nodeConfig = config.getNodeConfig();
        RemoteClusterExecutor remoteClusterExecutor = (RemoteClusterExecutor) this.executor;
        TaskResult<Channel> task = remoteClusterExecutor.openConnection(nodeConfig.getAddress()).syncUninterruptedly().get();

        switch (task.getState()) {
            case NULL:
                for (String line : Translation.listOf("remote.client.connection.failed", nodeConfig.getAddress())) {
                    this.logger.warn(line);
                }
                scheduledExecutor.schedule(() -> connectToHeadNode(config), 10, TimeUnit.SECONDS);
                break;
            case ERROR:
                for (String line : Translation.listOf("remote.client.connection.failed", nodeConfig.getAddress())) {
                    this.logger.warn(line);
                }
                if (task.getError() != null) {
                    this.logger.warn(Translation.of("global.error.prefix", task.getError().getMessage()));
                }
                scheduledExecutor.schedule(() -> connectToHeadNode(config), 10, TimeUnit.SECONDS);
                break;
            case SUCCESS:
                this.logger.info(Translation.of("remote.client.connection.success", nodeConfig.getAddress()));

                this.configManager.readConfig();
                connectTask.setResult(task.getResult());
                break;
        }
        return connectTask;
    }


    private IConfig loadConfig() {
        this.configManager = DriverUtility.get((this.configManager == null), new NodeConfigManager(), this.configManager);
        return loadConfig(this.configManager.isRemote());
    }

    private IConfig loadConfig(boolean remote) {
        //loading config
        this.configManager = DriverUtility.get((this.configManager == null), new NodeConfigManager(), this.configManager);
        IConfig config;
        if (remote) {
            config = this.configManager.readBaseConfig();
            this.logger.setMinLevel(INFO);
        } else {
            INetworkConfig networkConfig = this.configManager.readConfig();
            config = networkConfig;
            this.logger.setMinLevel(networkConfig.getLogLevel());
        }
        this.logger.info(Translation.of("global.logger.changed.level"), this.logger.getMinLevel().getName());
        return config;
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        if (component.matches(this.node)) {
            this.logger.info(message, args);
            return;
        }
        PacketDriverLogging packet = new PacketDriverLogging(component, message);
        this.executor.sendPacketToAll(packet);
    }

    private void initRemoteConfig(NodeRemoteSetup setup) throws IOException {
        IConfig baseConfig = configManager.getBaseConfig();
        INodeConfig nodeConfig = baseConfig.getNodeConfig();

        String host = setup.getHost();
        int port = setup.getPort();
        String authKey = setup.getAuthKey();

        nodeConfig.setAuthKey(authKey);
        nodeConfig.setClusterAddresses(new ProtocolAddress[]{new ProtocolAddress(host, port, authKey)});

        nodeConfig.setNodeName(setup.getName());
        nodeConfig.setAddress(new ProtocolAddress(host, port));
        nodeConfig.setMemory(setup.getMemory());
        nodeConfig.setRemote(true);

        baseConfig.setNodeConfig(nodeConfig);

        switch (setup.getDatabaseType()) {
            case SQLITE:
            case FILE:
                baseConfig.setDatabaseConfig(initDatabase(setup.getDatabaseType(), null, null));
                break;
            case MYSQL:
                new MySqlSetup(NodeDriver.getInstance().getConsole()).start((mySqlSetup, setupControlState1) -> {
                    if (setupControlState1 != SetupControlState.FINISHED) {
                        return;
                    }
                    baseConfig.setDatabaseConfig(initDatabase(setup.getDatabaseType(), mySqlSetup, null));
                });
                break;
            case MONGODB:
                new MongoDBSetup(NodeDriver.getInstance().getConsole()).start((mongoDBSetup, setupControlState1) -> {
                    if (setupControlState1 != SetupControlState.FINISHED) {
                        return;
                    }
                    baseConfig.setDatabaseConfig(initDatabase(setup.getDatabaseType(), null, mongoDBSetup));
                });
                break;
        }

        configManager.save(baseConfig);


        for (String line : Translation.listOf("node.setup.completed")) {
            this.logger.info(line);
        }
        sleep(1000L);
        System.exit(0);

    }

    private IDatabaseConfig initDatabase(DatabaseType databaseType, MySqlSetup mySqlSetup, MongoDBSetup mongoDBSetup) {
        IDatabaseConfig databaseConfiguration = new UniversalDatabaseConfig();

        String databaseHost = null;
        int databasePort = -1;
        String databaseUser = null;
        String databasePassword = null;
        String databaseName = null;
        String authDatabase = null;
        switch (databaseType) {
            case MYSQL:
                databaseHost = mySqlSetup.getDatabaseHost();
                databasePort = mySqlSetup.getDatabasePort();
                databaseUser = mySqlSetup.getDatabaseUser();
                databasePassword = mySqlSetup.getDatabasePassword();
                databaseName = mySqlSetup.getDatabaseName();
                authDatabase = "";
                break;
            case MONGODB:
                databaseHost = mongoDBSetup.getDatabaseHost();
                databasePort = mongoDBSetup.getDatabasePort();
                databaseUser = mongoDBSetup.getDatabaseUser();
                databasePassword = mongoDBSetup.getDatabasePassword();
                databaseName = mongoDBSetup.getDatabaseName();
                authDatabase = mongoDBSetup.getAuthDatabase();
                break;
        }

        databaseConfiguration.setHost(databaseHost);
        databaseConfiguration.setPort(databasePort);
        databaseConfiguration.setUser(databaseUser);
        databaseConfiguration.setPassword(databasePassword);
        databaseConfiguration.setDatabase(databaseName);
        databaseConfiguration.setAuthDatabase(authDatabase);
        databaseConfiguration.setType(databaseType);

        return databaseConfiguration;
    }

    private void initNodeConfig(NodeSetup setup) throws IOException {
        INetworkConfig config = configManager.getConfig();
        IDatabaseConfig databaseConfiguration = config.getDatabaseConfig();
        INodeConfig nodeConfig = config.getNodeConfig();

        FileUtils.copyResource("/impl/" + Constants.BRIDGE_FILE_NAME, CloudDriver.Constants.STORAGE_VERSIONS_FOLDER + "/" + Constants.BRIDGE_FILE_NAME, getClass());
        FileUtils.copyResource("/impl/" + Constants.REMOTE_FILE_NAME, CloudDriver.Constants.STORAGE_VERSIONS_FOLDER + "/" + Constants.REMOTE_FILE_NAME, getClass());
        this.logger.info(Translation.of("node.setup.copying.files",Constants.BRIDGE_FILE_NAME, Constants.REMOTE_FILE_NAME, Constants.STORAGE_VERSIONS_FOLDER));
        String nodeName = setup.getName();
        String host = setup.getHost();
        int port = setup.getPort();

        nodeConfig.setNodeName(nodeName);
        nodeConfig.setAddress(new ProtocolAddress(host, port));
        nodeConfig.setMemory(setup.getMemory());
        nodeConfig.setRemote(false);

        config.setNodeConfig(nodeConfig);


        switch (setup.getDatabaseType()) {
            case SQLITE:
            case FILE:

                config.setDatabaseConfig(initDatabase(setup.getDatabaseType(), null, null));
                break;
            case MYSQL:
                new MySqlSetup(NodeDriver.getInstance().getConsole()).start((mySqlSetup, setupControlState1) -> {
                    if (setupControlState1 != SetupControlState.FINISHED) {
                        return;
                    }
                    config.setDatabaseConfig(initDatabase(setup.getDatabaseType(), mySqlSetup, null));
                });
                break;
            case MONGODB:
                new MongoDBSetup(NodeDriver.getInstance().getConsole()).start((mongoDBSetup, setupControlState1) -> {
                    if (setupControlState1 != SetupControlState.FINISHED) {
                        return;
                    }
                    config.setDatabaseConfig(initDatabase(setup.getDatabaseType(), null, mongoDBSetup));
                });
                break;
        }

        configManager.setConfig(config);
        configManager.save();

        if (setup.isDefaultTasks()) {

            String[] args = new String[]{
                    "-XX:+UseG1GC",
                    "-XX:+ParallelRefProcEnabled",
                    "-XX:MaxGCPauseMillis=200",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+DisableExplicitGC",
                    "-XX:+AlwaysPreTouch",
                    "-XX:G1NewSizePercent=30",
                    "-XX:G1MaxNewSizePercent=40",
                    "-XX:G1HeapRegionSize=8M",
                    "-XX:G1ReservePercent=20",
                    "-XX:G1HeapWastePercent=5",
                    "-XX:G1MixedGCCountTarget=4",
                    "-XX:InitiatingHeapOccupancyPercent=15",
                    "-XX:G1MixedGCLiveThresholdPercent=90",
                    "-XX:G1RSetUpdatingPauseTimePercent=5",
                    "-XX:SurvivorRatio=32",
                    "-XX:+PerfDisableSharedMem",
                    "-XX:MaxTenuringThreshold=1",
                    "-Dusing.aikars.flags=https://mcflags.emc.gs",
                    "-Daikars.new.flags=true",
                    "-XX:-UseAdaptiveSizePolicy",
                    "-XX:CompileThreshold=100",
                    "-Dio.netty.recycler.maxCapacity=0",
                    "-Dio.netty.recycler.maxCapacity.default=0",
                    "-Djline.terminal=jline.UnsupportedTerminal"
            };

            this.databaseManager = new DefaultDatabaseManager(
                    setup.getDatabaseType(),
                    new UniversalDatabaseConfig(
                            setup.getDatabaseType(),
                            databaseConfiguration.getHost(),
                            databaseConfiguration.getPort(),
                            databaseConfiguration.getDatabase(),
                            databaseConfiguration.getAuthDatabase(),
                            databaseConfiguration.getUser(),
                            databaseConfiguration.getPassword()
                    )
            );

            LocalStorage database = this.databaseManager.getLocalStorage();
            database.registerSection("tasks", UniversalServiceTask.class);
            database.registerSection("groups", DefaultTaskGroup.class);

            NodeServiceTaskManager taskManager = new NodeServiceTaskManager();

            DefaultTaskGroup proxyGroup = new DefaultTaskGroup("Proxy", SpecificDriverEnvironment.PROXY, ServiceShutdownBehaviour.DELETE, args, new ArrayList<>(), Collections.singleton(new CloudTemplate("Proxy", "default", "local", true)));
            DefaultTaskGroup lobbyGroup = new DefaultTaskGroup("Lobby", SpecificDriverEnvironment.MINECRAFT, ServiceShutdownBehaviour.DELETE, args, new ArrayList<>(), Collections.singleton(new CloudTemplate("Lobby", "default", "local", true)));

            ServiceTask proxyTask = new UniversalServiceTask("Proxy", proxyGroup.getName(), Collections.singletonList(config.getNodeConfig().getNodeName().trim()), "Default HytoraCloud Service", "", 1024, 250, 1, -1, 0, 75, true, -1, new SimpleFallback(false, "", 0), ServiceVersion.BUNGEECORD, new ArrayList<>());
            ServiceTask lobbyTask = new UniversalServiceTask("Lobby", lobbyGroup.getName(), Collections.singletonList(config.getNodeConfig().getNodeName().trim()), "Default HytoraCloud Service", "", 512, 50, 1, -1, 1, 50, true, -1, new SimpleFallback(true, "", 1), ServiceVersion.SPIGOT_1_8_8, new ArrayList<>());
            lobbyTask.setProperty("gameServer", true);

            proxyTask.setProperty("onlineMode", true);
            proxyTask.setProperty("proxyProtocol", false);

            taskManager.addTaskGroup(proxyGroup);
            taskManager.addTaskGroup(lobbyGroup);

            taskManager.addTask(lobbyTask);
            taskManager.addTask(proxyTask);

            this.logger.info("§8");
            this.logger.info(Translation.of("node.setup.created.default.task"));
        } else {
            this.logger.info("§8");
        }

        for (String line : Translation.listOf("node.setup.completed")) {
            this.logger.info(line);
        }
        sleep(1000L);
        System.exit(0);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        if (!this.running) {
            return;
        }
        // TODO: 03.05.2022  migrating of head node
        if (this.nodeManager.isHeadNode() && this.nodeManager.getAllCachedNodes().size() > 1) {
            for (String line : Translation.listOf("node.shutdown.headnode.not.possible")) {
                this.logger.warn(line);
            }
            return;
        }

        // TODO: 12.05.2025 node remote shutdown values

        this.running = false;
        this.commandManager.setActive(false);


        this.logger.info(Translation.of("node.shutdown.start"));
        ShutdownAction[] shutdownActions = ShutdownAction.values();

        ///ProgressBar pb = new HytoraProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK, 65, shutdownActions);
        //pb.setPrinter(this.console);
        //pb.setTaskName("Initializing...");

        //pb.stepTo(0);
        sleep(500L);

        int i = 1;
        for (ShutdownAction shutdownAction : shutdownActions) {
            this.logger.info("  §8=> §8[§e{}§8/§e{}§8] §7" + Translation.of(shutdownAction.getMessage()), String.valueOf(i), String.valueOf(ShutdownAction.values().length));
            // pb.setTaskName(shutdownAction.getMessage());
            //pb.step();

            shutdownAction.getHandler().accept(this);
            sleep(shutdownAction.getSleepTime());
            i++;
        }
        sleep(100);
        this.logger.info(Translation.of("node.shutdown.message.success"));
        // pb.close("§aSuccessfully exited the CloudSystem§8!");
        System.exit(0);
    }

}
