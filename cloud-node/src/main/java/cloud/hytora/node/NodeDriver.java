package cloud.hytora.node;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.DriverStatus;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.DefaultCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;


import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.http.impl.NettyHttpServer;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.packets.services.ServiceForceShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.node.impl.handler.packet.normal.*;
import cloud.hytora.node.impl.handler.packet.remote.NodeRemoteLoggingHandler;
import cloud.hytora.node.impl.handler.packet.remote.NodeRemoteServerStartHandler;
import cloud.hytora.node.impl.handler.packet.remote.NodeRemoteServerStopHandler;
import cloud.hytora.node.impl.handler.packet.remote.NodeRemoteShutdownHandler;
import cloud.hytora.node.impl.handler.packet.normal.NodeDataCycleHandler;
import cloud.hytora.node.impl.handler.packet.normal.NodeLoggingPacketHandler;
import cloud.hytora.node.impl.handler.packet.normal.NodeStoragePacketHandler;
import cloud.hytora.node.impl.module.NodeModuleManager;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.node.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;

import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.NodeServiceInfo;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.ServiceTaskManager;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.task.DefaultServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.handler.http.V1PingRouter;
import cloud.hytora.node.impl.handler.http.V1StatusRouter;
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
import cloud.hytora.node.impl.database.IDatabaseManager;
import cloud.hytora.node.impl.database.def.DefaultDatabaseManager;
import cloud.hytora.node.service.NodeServiceTaskManager;
import cloud.hytora.node.impl.node.HytoraNode;
import cloud.hytora.node.impl.player.NodePlayerManager;
import cloud.hytora.node.service.helper.NodeServiceQueue;


import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Setter
public class NodeDriver extends CloudDriver implements Node {

    @Getter
    private static NodeDriver instance;

    private final ConfigManager configManager;
    private final Console console;
    private final CommandManager commandManager;
    private final CommandSender commandSender;
    private final DriverStorage storage;
    private INodeConfig config;

    private IDatabaseManager databaseManager;
    private ServiceTaskManager serviceTaskManager;
    private ServiceManager serviceManager;
    private PlayerManager playerManager;
    private ModuleManager moduleManager;
    private ChannelMessenger channelMessenger;
    private NodeManager nodeManager;
    private HttpServer webServer;

    private HytoraNode executor;
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



    public NodeDriver(Logger logger, Console console) throws Exception {
        super(logger, DriverEnvironment.NODE);
        instance = this;

        this.running = true;
        this.console = console;

        //loading config
        this.configManager = new ConfigManager();
        this.configManager.read();
        this.config = this.configManager.getConfig().getNodeConfig();

        this.logger.setMinLevel(this.configManager.getConfig().getLogLevel());
        this.logger.debug("Set LogLevel to {}", this.logger.getMinLevel().getName());

        //storage managing
        this.storage = new NodeDriverStorage();
        this.storage.fetch();

        //loading console
        this.console.addInputHandler(s -> CloudDriver.getInstance().getCommandManager().executeCommand(CloudDriver.getInstance().getCommandSender(), s));

        this.commandSender = new DefaultCommandSender(this.getConfig().getNodeName(), this.console);
        this.commandManager = new NodeCommandManager();

        //checking if setup required
        if (!this.configManager.isDidExist()) {
            this.startSetup();
            return;
        } else {
            this.logger.trace("Setup already done ==> Skipping...");
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

        //starting web-server
        this.webServer = new NettyHttpServer(config.getSslConfiguration());
        for (ProtocolAddress address : config.getHttpListeners()) {
            this.webServer.addListener(address);
        }

        //registering default web api handlers
        this.webServer.getHandlerRegistry().registerHandlers("v1", new V1PingRouter(), new V1StatusRouter());

        this.executor = new HytoraNode(this.configManager.getConfig());

        if (this.config.getClusterAddresses() != null && this.config.getClusterAddresses().length > 0) {
            this.config.markAsRemote();
        }

        if (this.config.isRemote()) {
            this.logger.info("This Node is a SubNode and will now connect to all provided Nodes in Cluster...");
            ProtocolAddress[] clusterAddresses = this.config.getClusterAddresses();
            for (ProtocolAddress address : clusterAddresses) {
                this.executor.connectToOtherNode(address.getAuthKey(), this.config.getNodeName(), address.getHost(), address.getPort(), DocumentFactory.emptyDocument()).addUpdateListener(b -> {
                    if (b.isSuccess()) {
                        this.logger.info("Successfully connected to §a" + address);
                    }
                });
            }
        } else {
            this.logger.info("§7This Node is a HeadNode §7and boots up the Cluster...");
        }


        //creating needed files
        this.logger.trace("Creating needed folders...");
        NodeDriver.NODE_FOLDER.mkdirs();

        NodeDriver.STORAGE_FOLDER.mkdirs();
        NodeDriver.STORAGE_VERSIONS_FOLDER.mkdirs();

        NodeDriver.SERVICE_DIR.mkdirs();
        NodeDriver.SERVICE_DIR_STATIC.mkdirs();
        NodeDriver.SERVICE_DIR_DYNAMIC.mkdirs();
        this.logger.trace("Required folders created!");

        this.databaseManager = new DefaultDatabaseManager(MainConfiguration.getInstance().getDatabaseConfiguration().getType());

        SectionedDatabase database = this.databaseManager.getDatabase();
        database.registerSection("players", DefaultCloudOfflinePlayer.class);
        database.registerSection("tasks", DefaultServiceTask.class);
        database.registerSection("groups", DefaultTaskGroup.class);

        this.serviceTaskManager = new NodeServiceTaskManager();
        this.serviceManager = new NodeServiceManager();
        this.playerManager = new NodePlayerManager(this.eventManager);
        this.channelMessenger = new NodeChannelMessenger(executor);
        this.nodeManager = new NodeNodeManager();
        this.moduleManager = new NodeModuleManager();
        this.logger.info("§8");

        //managing and loading modules
        this.moduleManager.setModulesDirectory(MODULE_FOLDER.toPath());
        this.moduleManager.resolveModules();
        this.moduleManager.loadModules();

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
        this.commandManager.registerParser(ServiceInfo.class, this.serviceManager::getServiceByNameOrNull);
        this.commandManager.registerParser(ServiceTask.class, this.serviceTaskManager::getTaskByNameOrNull);
        this.commandManager.registerParser(CloudPlayer.class, this.playerManager::getCloudPlayerByNameOrNull);
        this.commandManager.registerParser(Node.class, this.nodeManager::getNodeByNameOrNull);

        this.logger.trace("Registered " + this.commandManager.getCommands().size() + " Commands & " + this.commandManager.getParsers().size() + " Parsers!");
        this.logger.trace("§8");

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

        //remote packet handlers
        this.executor.registerRemoteHandler(new NodeRemoteShutdownHandler());
        this.executor.registerRemoteHandler(new NodeRemoteServerStartHandler());
        this.executor.registerRemoteHandler(new NodeRemoteServerStopHandler());
        this.executor.registerRemoteHandler(new NodeRemoteLoggingHandler());

        this.logger.trace("Registered " + PacketProvider.getRegisteredPackets().size() + " Packets & " + this.executor.getRegisteredPacketHandlers().size() + " PacketHandlers.");
        this.logger.trace("§8");

        //heart-beat execution for time out checking
        TimeOutChecker check = new TimeOutChecker();
        scheduledExecutor.scheduleAtFixedRate(check, 1, 1, TimeUnit.SECONDS);

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
        scheduledExecutor.scheduleAtFixedRate(() -> executor.sendPacketToAll(new NodeCycleDataPacket(this.config.getNodeName(), getLastCycleData())), 1_000, NodeCycleData.PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);
        scheduledExecutor.scheduleAtFixedRate(() -> this.executor.getClient("Application").ifPresent(DriverUpdatePacket::publishUpdate), 1_000, 1, TimeUnit.SECONDS);

        // add a shutdown hook for fast closes
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        if (component.matches(this)) {
            this.logger.info(message, args);
            return;
        }
        DriverLoggingPacket packet = new DriverLoggingPacket(component, message);
        this.executor.sendPacketToAll(packet);
    }


    public void reload() {
        logger.info("Reloading..");

        logger.info("Loading translations..");


        // TODO send files to other nodes on reload
        logger.info("Reloading modules..");
        moduleManager.disableModules();
        moduleManager.unregisterModules();
        moduleManager.resolveModules();
        moduleManager.loadModules();
        moduleManager.enableModules();

        logger.info("Reloading complete");
    }

    private void startSetup() {
        new NodeSetup(NodeDriver.getInstance().getConsole()).start((setup, setupControlState) -> {

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
            new NodeRemoteSetup(NodeDriver.getInstance().getConsole()).start((setup1, state) -> {
                if (state == SetupControlState.FINISHED) {
                    String host1 = setup1.getHost();
                    int port1 = setup1.getPort();
                    String authKey = setup1.getAuthKey();

                    nodeConfig.setAuthKey(authKey);
                    nodeConfig.setClusterAddresses(new ProtocolAddress[]{new ProtocolAddress(host1, port1)});
                }
            });
            nodeConfig.setHttpListeners(new ProtocolAddress[0]);
        }

        nodeConfig.setNodeName(nodeName);
        nodeConfig.setBindAddress(host);
        nodeConfig.setBindPort(port);
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

        this.logger.info("§7You §acompleted §7the NodeSetup§8!");
        this.logger.info("Please reboot the Node now to apply all changes!");
        System.exit(0);
    }

    @Override
    public List<ServiceInfo> getRunningServers() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getTask().getNode().equalsIgnoreCase(this.config.getNodeName());
        }).collect(Collectors.toList());
    }

    @Override
    public NodeCycleData getLastCycleData() {
        return NodeCycleData.current();
    }

    @Override
    public void setLastCycleData(NodeCycleData data) {
    }

    @Override
    public void log(String message, Object... args) {
        this.logger.info(message, args);
    }

    @Override
    public void stopServer(ServiceInfo server) {
        server.sendPacket(new ServiceForceShutdownPacket(server.getName()));
        Task.runTaskLater(() -> {
            Process process = server.asCloudServer().getProcess();
            if (process == null) {
                return;
            }
            process.destroyForcibly();
        }, TimeUnit.MILLISECONDS, 200);
    }

    @Override
    public void startServer(ServiceInfo server) {
        CloudDriver.getInstance().getServiceManager().startService(server);
    }

    @Override
    public String getName() {
        return this.getConfig().getNodeName();
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.NODE;
    }

    @Override
    public void sendPacket(Packet packet) {
        this.executor.sendPacketToAll(packet);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                buf.readString();
                buf.readEnum(ConnectionType.class);
                config = buf.readObject(DefaultNodeConfig.class);
                buf.readObject(NodeCycleData.class);
                break;

            case WRITE:
                buf.writeString(getName());
                buf.writeEnum(ConnectionType.NODE);
                buf.writeObject(config);
                buf.writeObject(getLastCycleData());
                break;
        }

    }


    @Override
    public void shutdown() {
        if (!this.running) {
            return;
        }
        // TODO: 03.05.2022  migrating of head node
        if (this.nodeManager.isHeadNode() && this.nodeManager.getAllConnectedNodes().size() > 0) {
            this.logger.warn("§eThis Node is the §cHeadNode §eright now and it's not possible for HeadNodes to shutdown because the migration of SubNodes to HeadNodes is not finished yet!");
            this.logger.warn("Make sure to shutdown every other Node first and then shutdown this Node!");
            return;
        }

        this.running = false;


        this.logger.info("§7Trying to terminate the §cCloudsystem§8...");

        this.moduleManager.disableModules();
        this.moduleManager.unregisterModules();

        this.webServer.shutdown();

        //shutting down servers
        for (ServiceInfo service : new ArrayList<>(this.serviceManager.getAllCachedServices())) {
            NodeServiceInfo cloudServer = service.asCloudServer();
            cloudServer.shutdown();
        }

        //Shutting down networking and database
        Task.multiTasking(this.executor.shutdown(), this.databaseManager.shutdown()).addUpdateListener(wrapper -> {
            Task.runTaskLater(() -> {
                FileUtils.delete(NodeDriver.SERVICE_DIR_DYNAMIC.toPath());
                FileUtils.delete(NodeDriver.STORAGE_TEMP_FOLDER.toPath());

                logger.info("§aSuccessfully exited the CloudSystem§8!");
                System.exit(0);
            }, TimeUnit.SECONDS, 2);
        });
    }

}
