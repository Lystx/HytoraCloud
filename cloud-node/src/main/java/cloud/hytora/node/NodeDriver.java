package cloud.hytora.node;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.DriverStatus;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.DefaultCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;


import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.http.impl.NettyHttpServer;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.permission.PermissionChecker;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.uuid.DriverUUIDCache;
import cloud.hytora.node.console.NodeScreenManager;
import cloud.hytora.node.impl.NodeUUIDCache;
import cloud.hytora.node.impl.handler.packet.normal.*;
import cloud.hytora.node.impl.handler.packet.remote.*;
import cloud.hytora.node.impl.handler.packet.normal.NodeDataCycleHandler;
import cloud.hytora.node.impl.handler.packet.normal.NodeLoggingPacketHandler;
import cloud.hytora.node.impl.handler.packet.normal.NodeStoragePacketHandler;
import cloud.hytora.node.impl.module.NodeModuleManager;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.node.packet.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.IProcessCloudServer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.ServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.UniversalServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.node.impl.handler.http.V1PingRouter;
import cloud.hytora.node.impl.handler.http.V1StatusRouter;
import cloud.hytora.node.impl.node.BaseNode;
import cloud.hytora.node.impl.setup.NodeRemoteSetup;
import cloud.hytora.node.service.template.LocalTemplateStorage;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.node.impl.command.*;
import cloud.hytora.node.impl.command.impl.*;
import cloud.hytora.node.impl.database.config.DatabaseType;
import cloud.hytora.node.impl.message.NodeChannelMessenger;
import cloud.hytora.node.impl.node.NodeNodeManager;
import cloud.hytora.node.impl.setup.database.MongoDBSetup;
import cloud.hytora.node.impl.setup.database.MySqlSetup;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.impl.setup.NodeSetup;
import cloud.hytora.node.impl.config.ConfigManager;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.driver.command.Console;
import cloud.hytora.node.impl.config.NodeDriverStorage;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.node.impl.database.def.DefaultDatabaseManager;
import cloud.hytora.node.service.NodeServiceTaskManager;
import cloud.hytora.node.impl.node.NodeBasedClusterExecutor;
import cloud.hytora.node.impl.player.NodePlayerManager;
import cloud.hytora.node.service.helper.NodeServiceQueue;


import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class NodeDriver extends CloudDriver<INode> {

    @Getter
    private static NodeDriver instance;

    private final ConfigManager configManager;
    private final Console console;
    private final CommandManager commandManager;
    private final CommandSender commandSender;

    private DriverUUIDCache uuidCache;
    private DriverStorage storage;

    private INode node;

    private IDatabaseManager databaseManager;
    private ServiceTaskManager serviceTaskManager;
    private ServiceManager serviceManager;
    private PlayerManager playerManager;
    private ModuleManager moduleManager;
    private ChannelMessenger channelMessenger;
    private NodeManager nodeManager;
    private HttpServer webServer;

    private NodeBasedClusterExecutor executor;
    private NodeServiceQueue serviceQueue;

    public static final File NODE_FOLDER = new File("local/");
    public static final File CONFIG_FILE = new File(NODE_FOLDER, "config.json");
    public static final File LOG_FOLDER = new File(NODE_FOLDER, "logs/");
    public static final File MODULE_FOLDER = new File(NODE_FOLDER, "modules/");

    public static final File STORAGE_FOLDER = new File(NODE_FOLDER, "storage/");
    public static final File STORAGE_VERSIONS_FOLDER = new File(STORAGE_FOLDER, "versions/");
    public static final File STORAGE_TEMP_FOLDER = new File(STORAGE_FOLDER, "tmp-" + UUID.randomUUID().toString().substring(0, 5) + "/");
    public static final File TEMPLATES_DIR = new File(STORAGE_FOLDER, "templates/");

    public static final File SERVICE_DIR = new File(NODE_FOLDER, "services/");
    public static final File SERVICE_DIR_STATIC = new File(SERVICE_DIR, "permanent/");
    public static final File SERVICE_DIR_DYNAMIC = new File(SERVICE_DIR, "temporary/");


    public NodeDriver(Logger logger, Console console, boolean devMode) throws Exception {
        super(logger, DriverEnvironment.NODE);
        instance = this;

        this.running = true;
        this.console = console;


        //setting node screen manager
        this.providerRegistry.setProvider(ScreenManager.class, new NodeScreenManager());

        ScreenManager screenManager = this.providerRegistry.getUnchecked(ScreenManager.class);
        screenManager.registerScreen("console", true);
        Screen consoleScreen = screenManager.getScreenByNameOrNull("console");
        consoleScreen.registerTabCompleter(buffer -> CloudDriver.getInstance().getCommandManager().completeCommand(CloudDriver.getInstance().getCommandSender(), buffer));
        screenManager.joinScreen(consoleScreen);


        //loading config
        this.configManager = new ConfigManager();
        this.configManager.read();

        this.logger.setMinLevel(this.configManager.getConfig().getLogLevel());
        this.logger.debug("Set LogLevel to {}", this.logger.getMinLevel().getName());

        //loading console
        this.console.addInputHandler(s -> CloudDriver.getInstance().getCommandManager().executeCommand(CloudDriver.getInstance().getCommandSender(), s));

        this.commandSender = new DefaultCommandSender(this.configManager.getConfig().getNodeConfig().getNodeName(), this.console).forceFunction((ExceptionallyConsumer<String>) s -> console.forceWrite(ColoredMessageFormatter.format(new LogEntry(Instant.now(), "node", s, LogLevel.INFO, null))));
        this.commandManager = new NodeCommandManager();

        //checking if setup required
        if (!this.configManager.isDidExist()) {

            new NodeSetup().start((setup, setupControlState) -> {

                if (setupControlState != SetupControlState.FINISHED) return;
                switch (setup.getDatabaseType()) {
                    case FILE:
                        initConfigs(setup, null, null);
                        break;
                    case MYSQL:
                        new MySqlSetup(NodeDriver.getInstance().getConsole()).start((mySqlSetup, setupControlState1) -> {
                            if (setupControlState1 != SetupControlState.FINISHED) return;
                            initConfigs(setup, mySqlSetup, null);
                        });
                        break;
                    case MONGODB:
                        new MongoDBSetup(NodeDriver.getInstance().getConsole()).start((mongoDBSetup, setupControlState1) -> {
                            if (setupControlState1 != SetupControlState.FINISHED) return;
                            initConfigs(setup, null, mongoDBSetup);
                        });
                        break;
                }

            });
            return;
        } else {
            this.logger.trace("Setup already done ==> Skipping...");
        }

        this.commandManager.setActive(true);

        if (devMode) {
            this.logger.debug("DevMode is activated!");
            //in dev mode player "Lystx" has every permission
            this.providerRegistry.setProvider(PermissionChecker.class, (playerUniqueId, permission) -> playerUniqueId.toString().equalsIgnoreCase("82e8f5a2-4077-407b-af8b-e8325cad7191"));
        }

        //avoid log4j errors
        org.apache.log4j.BasicConfigurator.configure(new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent loggingEvent) {
            }

            @Override
            public void close() {
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
        });

        DriverStatus status = status();

        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§b    __  __      __                   ________                __");
        this.logger.info("§b   / / / /_  __/ /_____  _________ _/ ____/ /___  __  ______/ /");
        this.logger.info("§b  / /_/ / / / / __/ __ \\/ ___/ __ `/ /   / / __ \\/ / / / __  / ");
        this.logger.info("§b / __  / /_/ / /_/ /_/ / /  / /_/ / /___/ / /_/ / /_/ / /_/ /  ");
        this.logger.info("§b/_/ /_/\\__, _____\\____/_/   \\__,_/\\____________/\\________,_/   ");
        this.logger.info("§b      /____/ ___/____ ___  __  _______/ __/  | |  / <  /       ");
        this.logger.info("§b ______    \\__ \\/ __ `__ \\/ / / / ___/ /_    | | / // /  ______");
        this.logger.info("§b/_____/   ___/ / / / / / / /_/ / /  / __/    | |/ // /  /_____/");
        this.logger.info("§b         /____/_/ /_/ /_/\\__,_/_/  /_/       |___//_/          ");
        this.logger.info("§8");
        this.logger.info("§bVersion §7: {}", (status.version() + " " + (status.experimental() ? "§8[§6Experimental§8]" : "§8[§aStable§8]")));
        this.logger.info("§bDeveloper(s) §7: {}", (Arrays.toString(status.developers()).replace("[", "").replace("]", "")));
        this.logger.info("§8==================================================");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8");

        this.node = new BaseNode(configManager);

        //starting web-server
        this.webServer = new NettyHttpServer();
        for (ProtocolAddress address : configManager.getConfig().getHttpListeners()) {
            this.webServer.addListener(address);
        }

        //registering default web api handlers
        this.webServer.getHandlerRegistry().registerHandlers("v1", new V1PingRouter(), new V1StatusRouter());

        this.executor = new NodeBasedClusterExecutor(this.configManager.getConfig());

        this.databaseManager = new DefaultDatabaseManager(MainConfiguration.getInstance().getDatabaseConfiguration().getType(), MainConfiguration.getInstance().getDatabaseConfiguration());
        this.providerRegistry.setProvider(IDatabaseManager.class, this.databaseManager);

        SectionedDatabase database = this.databaseManager.getDatabase();
        database.registerSection("players", DefaultCloudOfflinePlayer.class);
        database.registerSection("tasks", UniversalServiceTask.class);
        database.registerSection("groups", DefaultTaskGroup.class);

        this.serviceTaskManager = new NodeServiceTaskManager();
        this.serviceManager = new NodeServiceManager();
        this.playerManager = new NodePlayerManager(this.eventManager);
        this.channelMessenger = new NodeChannelMessenger(executor);
        this.nodeManager = new NodeNodeManager();
        this.moduleManager = new NodeModuleManager();
        this.logger.info("§8");

        if (node.getConfig().getClusterAddresses() != null && node.getConfig().getClusterAddresses().length > 0) {
            node.getConfig().setRemote();
        }

        if (this.node.getConfig().isRemote()) {
            this.executor.connectToAllOtherNodes(node.getName(), node.getConfig().getClusterAddresses()).syncUninterruptedly(); //wait till complete
        } else {
            this.logger.info("§7This Node is a HeadNode §7and boots up the Cluster...");
        }

        this.uuidCache = new NodeUUIDCache();
        this.uuidCache.setEnabled(MainConfiguration.getInstance().isUniqueIdCaching());
        this.uuidCache.loadAsync()
                .onTaskSucess(uuids -> logger.info("Loaded {} UUIDs from cache!", uuids.size()));


        //creating needed files
        this.logger.trace("Creating needed folders...");
        NodeDriver.NODE_FOLDER.mkdirs();

        NodeDriver.STORAGE_FOLDER.mkdirs();
        NodeDriver.STORAGE_VERSIONS_FOLDER.mkdirs();

        NodeDriver.SERVICE_DIR.mkdirs();
        NodeDriver.SERVICE_DIR_STATIC.mkdirs();
        NodeDriver.SERVICE_DIR_DYNAMIC.mkdirs();
        this.logger.trace("Required folders created!");


        //checking if directories got deleted meanwhile
        for (TaskGroup parent : this.serviceTaskManager.getAllTaskGroups()) {

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
        this.templateManager.registerStorage(new LocalTemplateStorage());

        //copying files
        this.logger.trace("§7Copying files§8...");
        FileUtils.copyResource("/impl/plugin.jar", STORAGE_VERSIONS_FOLDER + "/plugin.jar", getClass());
        FileUtils.copyResource("/impl/remote.jar", STORAGE_VERSIONS_FOLDER + "/remote.jar", getClass());

        //storage managing
        this.storage = new NodeDriverStorage();
        this.storage.fetch();

        this.logger.trace("Registering Commands & ArgumentParsers...");
        this.commandManager.registerCommand(new ShutdownCommand());
        this.commandManager.registerCommand(new HelpCommand());
        this.commandManager.registerCommand(new NodeCommand());
        this.commandManager.registerCommand(new TaskCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new ServiceCommand());
        this.commandManager.registerCommand(new PlayerCommand());
        this.commandManager.registerCommand(new TickCommand());
        this.commandManager.registerCommand(new ClusterCommand());
        this.commandManager.registerCommand(new LoggerCommand());

        //registering command argument parsers
        this.commandManager.registerParser(ServiceVersion.class, ServiceVersion::valueOf);
        this.commandManager.registerParser(LogLevel.class, LogLevel::valueOf);
        this.commandManager.registerParser(ICloudServer.class, this.serviceManager::getServiceByNameOrNull);
        this.commandManager.registerParser(IServiceTask.class, this.serviceTaskManager::getTaskByNameOrNull);
        this.commandManager.registerParser(ICloudPlayer.class, this.playerManager::getCloudPlayerByNameOrNull);
        this.commandManager.registerParser(CloudOfflinePlayer.class, this.playerManager::getOfflinePlayerByNameBlockingOrNull);
        this.commandManager.registerParser(INode.class, this.nodeManager::getNodeByNameOrNull);

        this.logger.trace("Registered " + this.commandManager.getCommands().size() + " Commands & " + this.commandManager.getParsers().size() + " Parsers!");
        this.logger.trace("§8");

        this.storage.set("cloud::messages", this.configManager.getConfig().getMessages());

        //registering packet handlers
        this.logger.trace("Registering Packets & Handlers...");
        this.executor.registerPacketHandler(new NodeRedirectPacketHandler());
        this.executor.registerPacketHandler(new NodeDataCycleHandler());
        this.executor.registerPacketHandler(new NodeOfflinePlayerPacketHandler());
        this.executor.registerPacketHandler(new NodeModulePacketHandler());
        this.executor.registerPacketHandler(new NodeModuleControllerPacketHandler());
        this.executor.registerPacketHandler(new NodeStoragePacketHandler());
        this.executor.registerPacketHandler(new NodeLoggingPacketHandler());
        this.executor.registerPacketHandler(new NodeServiceShutdownHandler());
        this.executor.registerPacketHandler(new NodePlayerCommandHandler());
        this.executor.registerPacketHandler(new NodeServiceConfigureHandler());

        //remote packet handlers
        this.executor.registerUniversalHandler(new NodeRemoteShutdownHandler());
        this.executor.registerUniversalHandler(new NodeRemoteServerStartHandler());
        this.executor.registerUniversalHandler(new NodeRemoteServerStopHandler());
        this.executor.registerUniversalHandler(new NodeRemoteLoggingHandler());
        this.executor.registerRemoteHandler(new NodeRemoteCacheHandler());

        this.logger.trace("Registered " + PacketProvider.getRegisteredPackets().size() + " Packets & " + this.executor.getRegisteredPacketHandlers().size() + " PacketHandlers.");
        this.logger.trace("§8");

        //heart-beat execution for time out checking
        TimeOutChecker check = new TimeOutChecker();
        scheduledExecutor.scheduleAtFixedRate(check, 1, 1, TimeUnit.SECONDS);

        //managing and loading modules
        this.moduleManager.setModulesDirectory(MODULE_FOLDER.toPath());
        this.moduleManager.resolveModules();
        this.moduleManager.loadModules();

        //enabling modules after having loaded the database
        this.moduleManager.enableModules();


        // print finish successfully message
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("This Node has successfully booted up and is now ready for personal use!");
        this.logger.info("=> Thanks for using HytoraCloud");
        this.logger.info("§8");
        this.logger.info("§8");

        //starting service queue
        this.serviceQueue = new NodeServiceQueue();

        //add node cycle data
        scheduledExecutor.scheduleAtFixedRate(() -> executor.sendPacketToAll(new NodeCycleDataPacket(this.node.getConfig().getNodeName(), this.node.getLastCycleData())), 1_000, NODE_PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);
        scheduledExecutor.scheduleAtFixedRate(() -> this.executor.getClient("Application").ifPresent(DriverUpdatePacket::publishUpdate), 1_000, 1, TimeUnit.SECONDS);

        // add a shutdown hook for fast closes
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        if (component.matches(this.node)) {
            this.logger.info(message, args);
            return;
        }
        DriverLoggingPacket packet = new DriverLoggingPacket(component, message);
        this.executor.sendPacketToAll(packet);
    }

    @Override
    public DriverUUIDCache getUUIDCache() {
        return uuidCache;
    }

    @Override
    public INode thisSidesClusterParticipant() {
        return this.node;
    }


    private void initConfigs(NodeSetup setup, MySqlSetup mySqlSetup, MongoDBSetup mongoDBSetup) throws IOException {
        MainConfiguration config = configManager.getConfig();
        DatabaseConfiguration databaseConfiguration = config.getDatabaseConfiguration();
        DefaultNodeConfig nodeConfig = config.getNodeConfig();

        String nodeName = setup.getName();
        String host = setup.getHost();
        int port = setup.getPort();
        boolean remote = setup.isRemote();

        if (remote) {
            config.setHttpListeners(new ProtocolAddress[0]);
            new NodeRemoteSetup(NodeDriver.getInstance().getConsole()).start((setup1, state) -> {
                if (state == SetupControlState.FINISHED) {
                    String host1 = setup1.getHost();
                    int port1 = setup1.getPort();
                    String authKey = setup1.getAuthKey();

                    nodeConfig.setAuthKey(authKey);
                    nodeConfig.setClusterAddresses(new ProtocolAddress[]{new ProtocolAddress(host1, port1, authKey)});
                }
            });
        }

        nodeConfig.setNodeName(nodeName);
        nodeConfig.setAddress(new ProtocolAddress(host, port));
        nodeConfig.setMemory(setup.getMemory());
        nodeConfig.setRemote(false);

        config.setNodeConfig(nodeConfig);

        DatabaseType databaseType = setup.getDatabaseType();
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
        config.setDatabaseConfiguration(databaseConfiguration);

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

            this.databaseManager = new DefaultDatabaseManager(databaseType, new DatabaseConfiguration(databaseType, databaseHost, databasePort, databaseName, authDatabase, databaseUser, databasePassword));

            SectionedDatabase database = this.databaseManager.getDatabase();
            database.registerSection("tasks", UniversalServiceTask.class);
            database.registerSection("groups", DefaultTaskGroup.class);

            NodeServiceTaskManager taskManager = new NodeServiceTaskManager();

            DefaultTaskGroup proxyGroup = new DefaultTaskGroup("Proxy", SpecificDriverEnvironment.PROXY, ServiceShutdownBehaviour.DELETE, args, new ArrayList<>(), Collections.singleton(new CloudTemplate("Proxy", "default", "local", true)));
            DefaultTaskGroup lobbyGroup = new DefaultTaskGroup("Lobby", SpecificDriverEnvironment.MINECRAFT, ServiceShutdownBehaviour.DELETE, args, new ArrayList<>(), Collections.singleton(new CloudTemplate("Lobby", "default", "local", true)));

            IServiceTask proxyTask = new UniversalServiceTask("Proxy", proxyGroup.getName(), Collections.singletonList(config.getNodeConfig().getNodeName()), "Default HytoraCloud Service", "", 1024, 250, 1, -1, 0, true, -1, new SimpleFallback(false, "", 0), ServiceVersion.BUNGEECORD, new ArrayList<>());
            IServiceTask lobbyTask = new UniversalServiceTask("Lobby", lobbyGroup.getName(), Collections.singletonList(config.getNodeConfig().getNodeName()), "Default HytoraCloud Service", "", 512, 50, 1, -1, 1, true, -1, new SimpleFallback(true, "", 1), ServiceVersion.SPIGOT_1_8_8, new ArrayList<>());
            lobbyTask.setProperty("gameServer", true);

            proxyTask.setProperty("onlineMode", true);
            proxyTask.setProperty("proxyProtocol", false);

            taskManager.addTask(lobbyTask);
            taskManager.addTask(proxyTask);
            taskManager.addTaskGroup(proxyGroup);
            taskManager.addTaskGroup(lobbyGroup);

            this.logger.info("Created default Proxy & Lobby ServiceTasks!");
            this.logger.info("§7You §acompleted §7the NodeSetup§8!");
            this.logger.info("Please reboot the Node now to apply all changes!");
            System.exit(0);
            return;
        }

        this.logger.info("§7You §acompleted §7the NodeSetup§8!");
        this.logger.info("Please reboot the Node now to apply all changes!");
        System.exit(0);
    }


    @Override
    public void shutdown() {
        if (!this.running) {
            return;
        }
        // TODO: 03.05.2022  migrating of head node
        if (this.nodeManager.isHeadNode() && this.nodeManager.getAllCachedNodes().size() > 1) {
            this.logger.warn("§eThis Node is the §cHeadNode §eright now and it's not possible for HeadNodes to shutdown because the migration of SubNodes to HeadNodes is not finished yet!");
            this.logger.warn("Make sure to shutdown every other Node first and then shutdown this Node!");
            return;
        }

        this.running = false;
        this.commandManager.setActive(false);


        this.logger.info("§7Trying to terminate the §cCloudsystem§8...");
        PlayerExecutor.forAll().disconnect("§cThe network was shut down!");

        Task.runTaskLater(() -> {


            this.moduleManager.disableModules();
            this.moduleManager.unregisterModules();

            this.webServer.shutdown();

            //shutting down servers
            for (ICloudServer service : new ArrayList<>(this.serviceManager.getAllCachedServices())) {
                IProcessCloudServer cloudServer = ((IProcessCloudServer) service);
                Process process = cloudServer.getProcess();
                if (process != null) {
                    process.destroyForcibly();
                }
            }

            Task.runSync(() -> logger.info("Terminating in §8[§c3§8]"));
            Task.runTaskLater(() -> logger.info("Terminating in §8[§c2§8]"), TimeUnit.SECONDS, 1);
            Task.runTaskLater(() -> logger.info("Terminating in §8[§c1§8]"), TimeUnit.SECONDS, 2);

            //Shutting down networking and database
            Task.multiTasking(this.executor.shutdown(), this.databaseManager.shutdown()).registerListener(wrapper -> {
                Task.runTaskLater(() -> {
                    FileUtils.delete(NodeDriver.SERVICE_DIR_DYNAMIC.toPath());
                    FileUtils.delete(NodeDriver.STORAGE_TEMP_FOLDER.toPath());

                    logger.info("§aSuccessfully exited the CloudSystem§8!");
                    System.exit(0);
                }, TimeUnit.SECONDS, 3);
            });
        }, TimeUnit.SECONDS, 1);
    }

}
