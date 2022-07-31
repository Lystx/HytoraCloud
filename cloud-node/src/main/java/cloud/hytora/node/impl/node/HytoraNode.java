package cloud.hytora.node.impl.node;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.server.ServiceClusterConnectEvent;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.cluster.client.SimpleClusterClientExecutor;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.packets.StorageUpdatePacket;
import cloud.hytora.driver.networking.packets.node.NodeConnectionDataRequestPacket;
import cloud.hytora.driver.networking.packets.node.NodeConnectionDataResponsePacket;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.node.NodeInfo;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public class HytoraNode extends ClusterExecutor {

    private final Map<ServiceInfo, Long> bootUpStatistics;
    private final String hostName;
    private final int port;
    private final List<PacketHandler<?>> remoteHandlers;

    public HytoraNode(MainConfiguration mainConfiguration) {
        super(NodeDriver.getInstance().getConfig().getAuthKey(), mainConfiguration.getNodeConfig().getNodeName());

        this.hostName = mainConfiguration.getNodeConfig().getBindAddress();
        this.port = mainConfiguration.getNodeConfig().getBindPort();
        this.bootUpStatistics = new ConcurrentHashMap<>();
        this.remoteHandlers = new ArrayList<>();

        this.bootAsync().handlePacketsAsync().openConnection(this.hostName, this.port)
                .addUpdateListener(wrapper -> {
                    if (wrapper.isSuccess()) {
                        NodeDriver.getInstance().getLogger().debug("Networking was successfully booted up and is ready to accept connections!");
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
                wrapper.overrideExecutor(HytoraNode.this).prepareSingleQuery().execute(packet).thenAccept(new Consumer<BufferedResponse>() {
                    @Override
                    public void accept(BufferedResponse response) {
                        DefaultNodeConfig nodeConfig = response.buffer().readObject(DefaultNodeConfig.class);
                        NodeCycleData data = response.buffer().readObject(NodeCycleData.class);

                        Node currentNode = new NodeInfo(NodeDriver.getInstance().getName(), ConnectionType.NODE, NodeDriver.getInstance().getConfig(), NodeDriver.getInstance().getLastCycleData());

                        if (CloudDriver.getInstance().getNodeManager().getNode(nodeConfig.getNodeName()).isPresent()) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(nodeConfig.getNodeName(), NodeConnectionDataResponsePacket.PayLoad.ALREADY_NODE_EXISTS, currentNode));
                            return;
                        } else if (getNodeName().equalsIgnoreCase(nodeConfig.getNodeName())) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(nodeConfig.getNodeName(), NodeConnectionDataResponsePacket.PayLoad.SAME_NAME_AS_HEAD_NODE, currentNode));
                            return;
                        }

                        String authKey = nodeConfig.getAuthKey();
                        if (authKey.equals(NodeDriver.getInstance().getConfig().getAuthKey())) {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(getNodeName(), NodeConnectionDataResponsePacket.PayLoad.SUCCESS, currentNode));

                            //right auth key -> registering node
                            Node node = new NodeInfo(executor.getName(), ConnectionType.NODE, nodeConfig, data);
                            CloudDriver.getInstance().getNodeManager().registerNode(node);

                            NodeDriver.getInstance().getServiceQueue().dequeue();

                        } else {
                            wrapper.sendPacket(new NodeConnectionDataResponsePacket(getNodeName(), NodeConnectionDataResponsePacket.PayLoad.WRONG_AUTH_KEY, currentNode));
                        }

                    }
                });
            } else {
                Task<Node> node = CloudDriver.getInstance().getNodeManager().getNode(executor.getName());
                node.ifPresent(CloudDriver.getInstance().getNodeManager()::unRegisterNode);
            }
        } else {
            if (state == ConnectionState.CONNECTED) {
                // set online
                ServiceInfo service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(executor.getName());
                if (service == null) {
                    //other remote connection

                    DriverUpdatePacket.publishUpdate(executor);

                    executor.sendPacket(new StorageUpdatePacket(
                            StorageUpdatePacket.StoragePayLoad.UPDATE,
                            CloudDriver.getInstance().getStorage().getRawData()
                    ));

                    if (executor.getName().equalsIgnoreCase("Application")) {
                        InetSocketAddress address = (InetSocketAddress) executor.getChannel().remoteAddress();
                        NodeDriver.getInstance().getLogger().info("§a==> Channel §8[§b" + executor.getName() + "@" + address.getHostName() + ":" + address.getPort() + "§8] §7connected");
                    }

                    return;
                }

                DriverUpdatePacket.publishUpdate(service);
                // update cache

                service.sendPacket(new StorageUpdatePacket(
                        StorageUpdatePacket.StoragePayLoad.UPDATE,
                        CloudDriver.getInstance().getStorage().getRawData()
                ));

                service.update();

                CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceClusterConnectEvent(service));

            } else {
                String service = executor.getName();
                if (service.equalsIgnoreCase("Application")) {
                    NodeDriver.getInstance().getLogger().warn("§a==> Channel §e{} - {} disconnected", "Cloud Application", executor.getChannel());
                    return;
                }
                NodeDriver base = NodeDriver.getInstance();
                ServiceInfo serviceInfo = base.getServiceManager().getServiceByNameOrNull(service);
                if (serviceInfo != null) {
                    CloudDriver.getInstance().getServiceManager().unregisterService(serviceInfo);
                } else {
                    NodeDriver.getInstance().getLogger().warn("§a==> Channel §e{} - {} tried to disconnect but no matching Service was found!", executor.getName(), executor.getChannel());
                }
            }

            Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                NodeDriver.getInstance().getServiceQueue().dequeue();
            }, 200L);
        }
    }

    private ClusterParticipant nodeAsClient;


    public Task<Boolean> connectToOtherNode(String authKey, String name, String hostname, int port, Document customData) {
        Task<Boolean> task = Task.empty();
        ClusterParticipant client = new ClusterParticipant(authKey, name, ConnectionType.NODE, customData) {

            @Override
            public void onAuthenticationChanged(PacketChannel wrapper) {
                ChannelHandlerContext context = wrapper.context();

                SimpleClusterClientExecutor connectedClient = (SimpleClusterClientExecutor) getConnectedClientByChannel(context.channel());
                connectedClient.setAuthenticated(true);
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


                for (PacketHandler packetHandler : new ArrayList<>(HytoraNode.this.remoteHandlers)) {
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


        client.registerPacketHandler((PacketHandler<NodeConnectionDataRequestPacket>) (wrapper1, packet) -> wrapper1.prepareResponse().buffer(buf -> buf.writeObject(NodeDriver.getInstance().getConfig()).writeObject(NodeCycleData.current())).execute(packet));
        client.registerPacketHandler((PacketHandler<NodeConnectionDataResponsePacket>) (wrapper12, packet) -> {
            NodeConnectionDataResponsePacket.PayLoad payLoad = packet.getPayLoad();
            String node = packet.getNode();
            switch (payLoad) {
                case SUCCESS:
                    Node nodeInfo = packet.getNodeInfo();
                    NodeDriver.getInstance().getNodeManager().registerNode(nodeInfo); //registering node that we connected to
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
        client.openConnection(hostname, port).addUpdateListener(wrap -> {
            if (wrap.isSuccess()) {
                task.setResult(true);
            } else {
                task.setFailure(wrap.error());
            }
        });
        nodeAsClient = client;
        return task;
    }

    @Override
    public void sendPacketToAll(IPacket packet) {
        if (NodeDriver.getInstance().getConfig().isRemote()) {
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

    public void registerStats(ServiceInfo service) {
        bootUpStatistics.put(service, System.currentTimeMillis());
    }

    public long getStats(ServiceInfo service) {
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
