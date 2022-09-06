package cloud.hytora.node.node;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.ITask;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.server.ServiceClusterConnectEvent;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.cluster.client.SimpleClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.StorageUpdatePacket;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.node.packet.NodeConnectionDataRequestPacket;
import cloud.hytora.driver.node.packet.NodeConnectionDataResponsePacket;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.data.DefaultNodeCycleData;
import cloud.hytora.driver.node.UniversalNode;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.config.MainConfiguration;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;


import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public class NodeBasedClusterExecutor extends ClusterExecutor {

    private final Map<ICloudServer, Long> bootUpStatistics;
    private final String hostName;
    private final int port;
    private final List<PacketHandler<?>> remoteHandlers;

    public NodeBasedClusterExecutor(MainConfiguration mainConfiguration) {
        super(NodeDriver.getInstance().getNode().getConfig().getAuthKey(), mainConfiguration.getNodeConfig().getNodeName());

        this.hostName = mainConfiguration.getNodeConfig().getAddress().getHost();
        this.port = mainConfiguration.getNodeConfig().getAddress().getPort();
        this.bootUpStatistics = new ConcurrentHashMap<>();
        this.remoteHandlers = new ArrayList<>();

        this.bootAsync().handlePacketsAsync().openConnection(this.hostName, this.port)
                .registerListener(wrapper -> {
                    if (wrapper.isSuccess()) {
                        NodeDriver.getInstance().getLogger().info("Networking was successfully booted up and is ready to accept connections!");
                    } else {
                        wrapper.error().printStackTrace();
                    }
                });
    }


    public <T extends AbstractPacket> void registerRemoteHandler(PacketHandler<T> handler) {
        this.remoteHandlers.add(handler);
    }

    public <T extends AbstractPacket> void registerUniversalHandler(PacketHandler<T> handler) {
        this.registerRemoteHandler(handler);
        this.registerPacketHandler(handler);
    }

    @Override
    public void handleConnectionChange(ConnectionState state, ClusterClientExecutor executor, PacketChannel wrapper) {
        ConnectionType type = executor.getType();

        if (type == ConnectionType.NODE) {

            if (state == ConnectionState.CONNECTED) {
                NodeConnectionDataRequestPacket packet = new NodeConnectionDataRequestPacket();
                wrapper.overrideExecutor(NodeBasedClusterExecutor.this).prepareSingleQuery().execute(packet).onTaskSucess(new Consumer<BufferedResponse>() {
                    @Override
                    public void accept(BufferedResponse response) {
                        DefaultNodeConfig nodeConfig = response.buffer().readObject(DefaultNodeConfig.class);
                        DefaultNodeCycleData data = response.buffer().readObject(DefaultNodeCycleData.class);

                        INode currentNode = new UniversalNode(NodeDriver.getInstance().getNode().getConfig(), NodeDriver.getInstance().getNode().getLastCycleData());

                        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getNode(nodeConfig.getNodeName()).isPresent()) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(nodeConfig.getNodeName(), NodeConnectionDataResponsePacket.PayLoad.ALREADY_NODE_EXISTS, currentNode));
                            return;
                        } else if (getNodeName().equalsIgnoreCase(nodeConfig.getNodeName())) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(nodeConfig.getNodeName(), NodeConnectionDataResponsePacket.PayLoad.SAME_NAME_AS_HEAD_NODE, currentNode));
                            return;
                        }

                        String authKey = nodeConfig.getAuthKey();
                        if (authKey.equals(NodeDriver.getInstance().getNode().getConfig().getAuthKey())) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(getNodeName(), NodeConnectionDataResponsePacket.PayLoad.SUCCESS, currentNode));

                            //right auth key -> registering node
                            INode node = new UniversalNode(nodeConfig, data);
                            NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).registerNode(node);

                            NodeDriver.getInstance().getServiceQueue().dequeue();

                        } else {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(getNodeName(), NodeConnectionDataResponsePacket.PayLoad.WRONG_AUTH_KEY, currentNode));
                        }

                    }
                });
            } else {
                ITask<INode> node = NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getNode(executor.getName());
                node.ifPresent(NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class)::unRegisterNode);
            }
        } else {
            if (state == ConnectionState.CONNECTED) {
                // set online
                ICloudServer service = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getService(executor.getName());
                if (service == null) {
                    //other remote connection

                    DriverUpdatePacket.publishUpdate(executor);

                    executor.sendPacket(new StorageUpdatePacket(
                            StorageUpdatePacket.StoragePayLoad.UPDATE,
                            CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).getRawData()
                    ));

                    if (executor.getName().equalsIgnoreCase("Application")) {
                        InetSocketAddress address = (InetSocketAddress) executor.getChannel().remoteAddress();
                        NodeDriver.getInstance().getLogger().info("§a==> Channel §8[§b" + executor.getName() + "@" + address.getHostName() + ":" + address.getPort() + "§8] §7connected");
                    }

                    return;
                }

                DriverUpdatePacket.publishUpdate(service);
                // updateTask cache

                service.sendPacket(new StorageUpdatePacket(
                        StorageUpdatePacket.StoragePayLoad.UPDATE,
                        CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).getRawData()
                ));

                service.update();

                CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new ServiceClusterConnectEvent(service));

            } else {
                String service = executor.getName();
                if (service.equalsIgnoreCase("Application")) {
                    NodeDriver.getInstance().getLogger().warn("§a==> Channel §e{} - {} disconnected", "Cloud Application", executor.getChannel());
                    return;
                }
                NodeDriver base = NodeDriver.getInstance();
                ICloudServer ICloudServer = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getService(service);
                if (ICloudServer != null) {
                    CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).unregisterService(ICloudServer);
                } else {
                    NodeDriver.getInstance().getLogger().warn("§a==> Channel §e{} - {} tried to disconnect but no matching Service was found!", executor.getName(), executor.getChannel());
                }
            }

            Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                NodeDriver.getInstance().getServiceQueue().dequeue();
            }, 200L);
        }
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).update();
    }

    private ClusterParticipant nodeAsClient;


    public ITask<Void> connectToAllOtherNodes(String name, ProtocolAddress... nodeAddresses) {
        return ITask.callAsync(() -> {

            Logger.constantInstance().info("This Node is a SubNode and will now connect to all provided Nodes in Cluster...");

            Iterator<ProtocolAddress> iterator = Arrays.stream(nodeAddresses).iterator();
            while (iterator.hasNext()) {
                ProtocolAddress address = iterator.next();
                if (this.connectToOtherNode(address.getAuthKey(), name, address.getHost(), address.getPort(), DocumentFactory.emptyDocument()).syncUninterruptedly().get()) {
                    Logger.constantInstance().info("Successfully connected to §a" + address);
                }
            }
            return null;
        });
    }

    public ITask<Boolean> connectToOtherNode(String authKey, String name, String hostname, int port, Document customData) {
        ITask<Boolean> task = ITask.empty();
        ClusterParticipant client = new ClusterParticipant(authKey, name, ConnectionType.NODE, customData) {

            @Override
            public void onAuthenticationChanged(PacketChannel wrapper) {
                ChannelHandlerContext context = wrapper.context();

                SimpleClusterClientExecutor connectedClient = (SimpleClusterClientExecutor) getConnectedClientByChannel(context.channel());
                connectedClient.setAuthenticated(true);
                task.setResult(true);
            }

            @Override
            public void onActivated(ChannelHandlerContext channelHandlerContext) {
                addConnectedClient(channelHandlerContext.channel());

                SimpleClusterClientExecutor connectedClient = (SimpleClusterClientExecutor) getConnectedClientByChannel(channelHandlerContext.channel());
                connectedClient.setName(getName());
                connectedClient.setType(ConnectionType.NODE);
            }

            @Override
            public void onClose(ChannelHandlerContext channelHandlerContext) {
                closeClient(channelHandlerContext);
            }

            @Override
            public <T extends IPacket> void handlePacket(PacketChannel wrapper, @NotNull T packet) {


                for (PacketHandler packetHandler : new ArrayList<>(NodeBasedClusterExecutor.this.remoteHandlers)) {
                    try {
                        packetHandler.handle(wrapper, packet);
                    } catch (Exception e) {
                        if (e instanceof ClassCastException) {
                            //not right handler
                            continue;
                        }
                        e.printStackTrace();
                    }
                }
                super.handlePacket(wrapper, packet);
            }
        };


        client.registerPacketHandler((PacketHandler<NodeConnectionDataRequestPacket>) (wrapper1, packet) -> wrapper1.prepareResponse().buffer(buf -> buf.writeObject(NodeDriver.getInstance().getNode().getConfig()).writeObject(DefaultNodeCycleData.current())).execute(packet));
        client.registerPacketHandler((PacketHandler<NodeConnectionDataResponsePacket>) (wrapper12, packet) -> {
            NodeConnectionDataResponsePacket.PayLoad payLoad = packet.getPayLoad();
            String node = packet.getNode();
            switch (payLoad) {
                case SUCCESS:
                    INode nodeInfo = packet.getNodeInfo();
                    NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).registerNode(nodeInfo); //registering node that we connected to
                    CloudDriver.getInstance().getLogger().info("This Node §asuccessfully §7connected to §b{}§8.", nodeInfo);
                    break;
                case WRONG_AUTH_KEY:
                    CloudDriver.getInstance().getLogger().error("You provided a wrong AuthKey for the Node to check! Check again and reboot the CloudSystem!");
                    break;
                case ALREADY_NODE_EXISTS:
                    CloudDriver.getInstance().getLogger().error("There is already a Node with the name §e{}", node);
                    break;
                case SAME_NAME_AS_HEAD_NODE:
                    CloudDriver.getInstance().getLogger().error("You can not name this Node like the HeadNode!");
                    break;
            }
        });
        client.openConnection(hostname, port).registerListener(wrap -> {
            if (!wrap.isSuccess()) {
                task.setFailure(wrap.error());
            }
        });
        nodeAsClient = client;
        return task;
    }

    @Override
    public void sendPacketToAll(IPacket packet) {
        if (NodeDriver.getInstance().getNode().getConfig().isRemote()) {
            nodeAsClient.sendPacket(packet);
        }
        super.sendPacketToAll(packet);
    }

    @Override
    public void sendPacket(IPacket packet) {
        if (this.nodeAsClient != null) {
            this.nodeAsClient.sendPacket(packet);
            return;
        }
        super.sendPacket(packet);
    }

    public void registerStats(ICloudServer service) {
        bootUpStatistics.put(service, System.currentTimeMillis());
    }

    public long getStats(ICloudServer service) {
        long time = System.currentTimeMillis() - bootUpStatistics.getOrDefault(service, (System.currentTimeMillis() - 1));
        bootUpStatistics.remove(service);
        return time;
    }

    @Override
    public void sendPacket(IPacket packet, NetworkComponent component) {
        this.getClient(component.getName()).ifPresent(c -> c.sendPacket(packet));
    }

    @Override
    public int getProxyStartPort() {
        return MainConfiguration.getInstance().getProxyStartPort();
    }

    @Override
    public int getSpigotStartPort() {
        return MainConfiguration.getInstance().getSpigotStartPort();
    }
}
