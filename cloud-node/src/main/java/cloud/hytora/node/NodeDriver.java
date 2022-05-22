package cloud.hytora.node;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.DriverStatus;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.DefaultCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.InternalDriverEventAdapter;


import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.http.impl.NettyHttpServer;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.node.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.NodeCloudServer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.configuration.ConfigurationManager;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.DefaultServerConfiguration;
import cloud.hytora.driver.services.configuration.bundle.ConfigurationParent;
import cloud.hytora.driver.services.configuration.bundle.DefaultConfigurationParent;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.handler.http.V1PingRouter;
import cloud.hytora.node.impl.handler.http.V1StatusRouter;
import cloud.hytora.node.service.template.LocalTemplateStorage;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.node.impl.command.*;
import cloud.hytora.node.impl.command.impl.*;
import cloud.hytora.node.impl.database.config.DatabaseType;
import cloud.hytora.node.impl.handler.*;
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
import cloud.hytora.node.service.NodeConfigurationManager;
import cloud.hytora.node.impl.node.HytoraNode;
import cloud.hytora.node.impl.player.NodePlayerManager;
import cloud.hytora.node.service.helper.NodeServiceQueue;


import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
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
    private final INodeConfig config;

    private IDatabaseManager databaseManager;
    private ConfigurationManager configurationManager;
    private ServiceManager serviceManager;
    private PlayerManager playerManager;
    private ChannelMessenger channelMessenger;
    private NodeManager nodeManager;
    private HttpServer webServer;

    private HytoraNode executor;
    private NodeServiceQueue serviceQueue;


    public static final File NODE_FOLDER = new File("local/");
    public static final File CONFIG_FILE = new File(NODE_FOLDER, "config.json");
    public static final File LOG_FOLDER = new File(NODE_FOLDER, "logs/");

    public static final File STORAGE_FOLDER = new File(NODE_FOLDER, "storage/");
    public static final File CONFIGURATIONS_FOLDER = new File(STORAGE_FOLDER, "configurations/");
    public static final File CONFIGURATIONS_PARENTS_FOLDER = new File(STORAGE_FOLDER, "groups/");
    public static final File STORAGE_VERSIONS_FOLDER = new File(STORAGE_FOLDER, "versions/");
    public static final File STORAGE_TEMP_FOLDER = new File(STORAGE_FOLDER, "tmp-" + UUID.randomUUID().toString().substring(0, 5) + "/");
    public static final File TEMPLATES_DIR = new File(STORAGE_FOLDER, "templates/");

    public static final File SERVICE_DIR = new File(NODE_FOLDER, "services/");
    public static final File SERVICE_DIR_STATIC = new File(SERVICE_DIR, "permanent/");
    public static final File SERVICE_DIR_DYNAMIC = new File(SERVICE_DIR, "temporary/");

    /**
     * If the node is still running
     */
    private boolean running;

    public NodeDriver(Logger logger, Console console) throws Exception {
        super(logger, DriverEnvironment.NODE);
        instance = this;

        this.running = true;
        this.console = console;

        //loading config
        this.configManager = new ConfigManager();
        this.configManager.read();
        this.config = this.configManager.getConfig().getNodeConfig();

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
        }

        DriverStatus status = status();
        this.logger.info("§8==================================================");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8» §bHytoraCloud §8| §7Where §3opportunity §7connects §8«");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8× §bVersion §7: §7" + status.version() + " " + (status.experimental() ? "§8[§6Experimental§8]" : "§8[§aStable§8]"));
        this.logger.info("§8× §bDeveloper(s) §7: §7" + Arrays.toString(status.developers()).replace("[", "").replace("]", ""));
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8==================================================");
        this.logger.info("§8");
        this.logger.info("§8");
        this.logger.info("§8");
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
            this.logger.info("§7This Node is a §eSubNode §7and will now connect to all provided Nodes in Cluster...");
            ProtocolAddress[] clusterAddresses = this.config.getClusterAddresses();
            for (ProtocolAddress address : clusterAddresses) {
                this.executor.connectToOtherNode(this.config.getNodeName(), address.getHost(), address.getPort(), DocumentFactory.emptyDocument()).addUpdateListener(b -> {
                    if (b.isSuccess()) {
                        this.logger.info("§aSuccessfully §7connected to §b" + address);
                    }
                });
            }
        } else {
            this.logger.info("§7This Node is a §aHeadNode §7and boots up the Cluster...");
        }


        //creating needed files
        NodeDriver.NODE_FOLDER.mkdirs();
        NodeDriver.CONFIGURATIONS_FOLDER.mkdirs();

        NodeDriver.STORAGE_FOLDER.mkdirs();
        NodeDriver.STORAGE_VERSIONS_FOLDER.mkdirs();

        NodeDriver.SERVICE_DIR.mkdirs();
        NodeDriver.SERVICE_DIR_STATIC.mkdirs();
        NodeDriver.SERVICE_DIR_DYNAMIC.mkdirs();

        //initializing managers
        new InternalDriverEventAdapter(this.eventManager, executor);
        this.databaseManager = new DefaultDatabaseManager(MainConfiguration.getInstance().getDatabaseConfiguration().getType());

        SectionedDatabase database = this.databaseManager.getDatabase();
        database.registerSection("players", DefaultCloudOfflinePlayer.class);
        database.registerSection("configurations", DefaultServerConfiguration.class);
        database.registerSection("groups", DefaultConfigurationParent.class);

        this.configurationManager = new NodeConfigurationManager();
        this.serviceManager = new NodeServiceManager();
        this.playerManager = new NodePlayerManager(this.eventManager);
        this.channelMessenger = new NodeChannelMessenger(executor);
        this.nodeManager = new NodeNodeManager();
        this.logger.info("§8");


        //checking if directories got deleted meanwhile
        for (ConfigurationParent parent : this.configurationManager.getAllParentConfigurations()) {

            //creating templates
            for (ServiceTemplate template : parent.getTemplates()) {
                TemplateStorage storage = template.getStorage();
                if (storage != null) {
                    storage.createTemplate(template);
                }
            }
        }

        //registering template storage
        this.templateManager.registerStorage(new LocalTemplateStorage());


        //copying files
        this.logger.info("§7Copying files§8...");
        FileUtils.copyResource("/impl/plugin.jar", STORAGE_VERSIONS_FOLDER + "/plugin.jar", getClass());
        FileUtils.copyResource("/impl/remote.jar", STORAGE_VERSIONS_FOLDER + "/remote.jar", getClass());

        this.logger.info("§7Registering §bCommands §8& §bArgumentParsers§8...");
        this.commandManager.registerCommand(new ShutdownCommand());
        this.commandManager.registerCommand(new HelpCommand());
        this.commandManager.registerCommand(new NodeCommand());
        this.commandManager.registerCommand(new ConfigurationCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new ServiceCommand());
        this.commandManager.registerCommand(new PlayerCommand());

        //registering command argument parsers
        this.commandManager.registerParser(ServiceVersion.class, ServiceVersion::valueOf);
        this.commandManager.registerParser(CloudServer.class, this.serviceManager::getServiceByNameOrNull);
        this.commandManager.registerParser(ServerConfiguration.class, this.configurationManager::getConfigurationByNameOrNull);
        this.commandManager.registerParser(CloudPlayer.class, this.playerManager::getCloudPlayerByNameOrNull);
        this.commandManager.registerParser(Node.class, this.nodeManager::getNodeByNameOrNull);

        this.logger.info("§a=> §7Registered §a" + this.commandManager.getCommands().size() + " Commands §8& §a" + this.commandManager.getParsers().size() + " Parsers§8!");
        this.logger.info("§8");

        //registering packet handlers
        this.logger.info("Registering §bPacketts §8& §bHandlers§8...");
        this.executor.registerPacketHandler(new NodeStoragePacketHandler());
        this.executor.registerPacketHandler(new NodeRedirectPacketHandler());
        this.executor.registerPacketHandler(new NodeServiceRemovePacketHandler());
        this.executor.registerPacketHandler(new NodeServiceAddPacketHandler());
        this.executor.registerPacketHandler(new NodeLoggingPacketHandler());
        this.executor.registerPacketHandler(new NodeDataCycleHandler());
        this.executor.registerPacketHandler(new NodeRemoteShutdownHandler());
        this.executor.registerPacketHandler(new NodeRemoteServerStartHandler());
        this.executor.registerPacketHandler(new NodeRemoteServerStopHandler());
        this.executor.registerPacketHandler(new NodeOfflinePlayerPacketHandler());

        this.logger.info("§a=> Registered §a" + PacketProvider.getRegisteredPackets().size() + " Packets §8& §a" + this.executor.getRegisteredPacketHandlers().size() + " Handlers§8.");
        this.logger.info("§8");

        // print finish successfully message
        this.logger.info("§7This Node has §asuccessfully §7booted up§8!");
        this.logger.info("§2==> Thanks for using HytoraCloud");
        this.logger.info("§8");

        //starting service queue
        this.serviceQueue = new NodeServiceQueue();
        this.serviceQueue.dequeue();

        //add node cycle data
        scheduledExecutor.scheduleAtFixedRate(() -> executor.sendPacket(new NodeCycleDataPacket(this.config.getNodeName(), getLastCycleData())), 1_000, NodeCycleData.PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);


        // add a shutdown hook for fast closes
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        DriverLoggingPacket packet = new DriverLoggingPacket(component, message);
        this.executor.sendPacketToAll(packet);
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

        this.webServer.shutdown();

        //shutting down servers
        for (CloudServer service : this.serviceManager.getAllCachedServices()) {
            NodeCloudServer cloudServer = service.asCloudServer();
            Process process = cloudServer.getProcess();
            if (process == null) {
                continue;
            }
            process.destroyForcibly();
        }
        //Shutting down networking and database
        Wrapper.multiTasking(this.executor.shutdown(), this.databaseManager.shutdown()).addUpdateListener(wrapper -> {


            FileUtils.delete(NodeDriver.SERVICE_DIR_DYNAMIC.toPath());
            FileUtils.delete(NodeDriver.STORAGE_TEMP_FOLDER.toPath());

            logger.info("§aSuccessfully exited the CloudSystem§8!");
            System.exit(0);
        });
    }

    private void startSetup() {
        new NodeSetup().start((setup, setupControlState) -> {

            if (setupControlState != SetupControlState.FINISHED) return;

            switch (setup.getDatabaseType()) {
                case FILE:
                    initConfigs(setup, null, null);
                    break;
                case MYSQL:
                    new MySqlSetup().start((mySqlSetup, setupControlState1) -> {
                        if (setupControlState1 != SetupControlState.FINISHED) return;
                        initConfigs(setup, mySqlSetup, null);
                    });
                    break;
                case MONGODB:
                    new MongoDBSetup().start((mongoDBSetup, setupControlState1) -> {
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
        int serviceStartPort = setup.getServiceStartPort();

        nodeConfig.setNodeName(nodeName);
        nodeConfig.setBindAddress(host);
        nodeConfig.setBindPort(port);
        nodeConfig.setRemote(false);

        config.setSpigotStartPort(serviceStartPort);
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
    public List<CloudServer> getRunningServers() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> {
            s.getConfiguration();
            return s.getConfiguration().getNode().equalsIgnoreCase(this.config.getNodeName());
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
    public void stopServer(CloudServer server) {
        CloudDriver.getInstance().getServiceManager().shutdownService(server);
    }

    @Override
    public void startServer(CloudServer server) {
        CloudDriver.getInstance().getServiceManager().startService(server).addUpdateListener(new Consumer<Wrapper<CloudServer>>() {
            @Override
            public void accept(Wrapper<CloudServer> iServiceWrapper) {
                if (iServiceWrapper.isSuccess()) {
                    CloudServer service = iServiceWrapper.get();

                    ClusterClientExecutor nodeClient = NodeDriver.getInstance().getExecutor().getClient(service.getConfiguration().getNode()).orElse(null);
                    boolean thisSidesNode = service.getConfiguration().getNode().equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName());

                    if (thisSidesNode) {
                        CloudDriver.getInstance().getLogger().info("§6==> §7This Node started §b" + service.getName());
                    } else {
                        CloudDriver.getInstance().getLogger().info("§6==> §7Node '" + nodeClient.getName() + "' §8(§b" + nodeClient.getChannel() + "§8) started §b" + service.getName());
                    }

                } else {
                    iServiceWrapper.error().printStackTrace();
                }
            }
        });
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
    public void sendPacket(IPacket packet) {
        this.executor.sendPacketToAll(packet);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                buf.readString();
                buf.readEnum(ConnectionType.class);
                break;

            case WRITE:
                buf.writeString(getName());
                buf.writeEnum(ConnectionType.NODE);
                break;
        }
    }

}
