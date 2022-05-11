package cloud.hytora.node;

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
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.NodeCloudServer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.configuration.ConfigurationManager;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.node.impl.command.*;
import cloud.hytora.node.impl.command.impl.*;
import cloud.hytora.node.impl.database.DatabaseType;
import cloud.hytora.node.impl.handler.*;
import cloud.hytora.node.impl.message.NodeChannelMessenger;
import cloud.hytora.node.impl.node.NodeNodeManager;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.service.template.NodeTemplateService;
import cloud.hytora.node.impl.setup.NodeSetup;
import cloud.hytora.node.impl.config.ConfigManager;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.driver.command.Console;
import cloud.hytora.node.impl.config.NodeDriverStorage;
import cloud.hytora.node.impl.database.DatabaseConfiguration;
import cloud.hytora.node.impl.database.IDatabaseManager;
import cloud.hytora.node.impl.database.impl.DatabaseManager;
import cloud.hytora.node.service.NodeConfigurationManager;
import cloud.hytora.node.impl.node.HytoraNode;
import cloud.hytora.node.impl.player.NodePlayerManager;
import cloud.hytora.node.service.helper.NodeServiceQueue;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private HytoraNode executor;
    private NodeServiceQueue serviceQueue;
    private NodeTemplateService nodeTemplateService;

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

        //initializing managers
        new InternalDriverEventAdapter(this.eventManager, executor);
        this.databaseManager = new DatabaseManager(MainConfiguration.getInstance().getDatabaseConfiguration().getType());
        this.configurationManager = new NodeConfigurationManager();
        this.serviceManager = new NodeServiceManager();
        this.nodeTemplateService = new NodeTemplateService();
        this.playerManager = new NodePlayerManager(this.eventManager);
        this.channelMessenger = new NodeChannelMessenger(executor);
        this.nodeManager = new NodeNodeManager();
        this.logger.info("§8");

        // registered commands
        this.logger.info("§7Registering §bCommands §8& §bArgumentParsers§8...");
        this.commandManager.registerCommand(new ShutdownCommand());
        this.commandManager.registerCommand(new HelpCommand());
        this.commandManager.registerCommand(new NodeCommand());
        this.commandManager.registerCommand(new ConfigurationCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new ServiceCommand());

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

        //shutting down servers
        for (CloudServer service : this.serviceManager.getAllCachedServices()) {
            NodeCloudServer cloudServer = service.asCloudServer();
            Process process = cloudServer.getProcess();
            process.destroyForcibly();
        }
        //Shutting down networking and database
        Wrapper.multiTasking(this.executor.shutdown(), this.databaseManager.shutdown()).addUpdateListener(wrapper -> {

            try {
                FileUtils.deleteDirectory(new File("tmp"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("§aSuccessfully exited the CloudSystem§8!");
            System.exit(0);
        });
    }

    private void startSetup() {
        new NodeSetup().start((setup, setupControlState) -> {

            MainConfiguration config = configManager.getConfig();
            DatabaseConfiguration databaseConfiguration = config.getDatabaseConfiguration();
            DefaultNodeConfig nodeConfig = config.getNodeConfig();

            if (setupControlState == SetupControlState.FINISHED) {
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
                if (databaseType != DatabaseType.FILE) {
                    String databaseHost = setup.getDatabaseHost();
                    int databasePort = setup.getDatabasePort();
                    String databaseUser = setup.getDatabaseUser();
                    String databasePassword = setup.getDatabasePassword();
                    String databaseName = setup.getDatabaseName();

                    databaseConfiguration.setHost(databaseHost);
                    databaseConfiguration.setPort(databasePort);
                    databaseConfiguration.setUser(databaseUser);
                    databaseConfiguration.setPassword(databasePassword);
                    databaseConfiguration.setDatabase(databaseName);
                }

                databaseConfiguration.setType(databaseType);
                config.setDatabaseConfiguration(databaseConfiguration);

                configManager.setConfig(config);
                configManager.save();

                this.logger.info("§7You §acompleted §7the NodeSetup§8!");
                this.logger.info("Please reboot the Node now to apply all changes!");
                System.exit(0);
            }

        });
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
    public void setLastCycleData(NodeCycleData data) {}

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
