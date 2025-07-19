package cloud.hytora.node.impl.node;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.config.IConfig;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeRegister;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeUnregister;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceConnect;
import cloud.hytora.driver.language.Translation;
import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.types.ConnectionState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.entity.node.UniversalNode;
import cloud.hytora.driver.config.def.UniversalNodeConfig;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public class NodeBasedClusterExecutor extends ClusterExecutor implements Cluster {

    private final Map<String, Long> bootUpStatistics;
    private final String hostName;
    private final int port;

    public NodeBasedClusterExecutor(IConfig networkConfig) {
        super(NodeDriver.getInstance().getNode().getConfig().getAuthKey(), networkConfig.getNodeConfig().getNodeName());

        this.hostName = networkConfig.getNodeConfig().getAddress().getHost();
        this.port = networkConfig.getNodeConfig().getAddress().getPort();
        this.bootUpStatistics = new ConcurrentHashMap<>();

        //.bootAsync().handlePacketsAsync();
        this.openConnection(this.hostName, this.port)
                .registerListener(wrapper -> {
                    if (wrapper.isSuccess()) {
                        NodeDriver.getInstance().getLogger().debug(Translation.of("node.client.connection.opened"));
                    } else {
                        wrapper.error().printStackTrace();
                    }
                });
    }


    @Override
    public void handleConnectionChange(ConnectionState state, PacketChannel channel) {
        ConnectionType type = channel.getType();

        if (type == ConnectionType.NODE) {

            if (state == ConnectionState.CONNECTED) {
                PacketCloudEntityNode packet = PacketCloudEntityNode.forDataRequest();
                this.getPacketChannel().sendQuery().execute(packet).onTaskSucess(new Consumer<BufferedResponse>() {
                    @Override
                    public void accept(BufferedResponse response) {
                        UniversalNodeConfig nodeConfig = response.buffer().readObject(UniversalNodeConfig.class);
                        DefaultNodeData data = response.buffer().readObject(DefaultNodeData.class);

                        INode currentNode = new UniversalNode(NodeDriver.getInstance().getNode().getConfig(), NodeDriver.getInstance().getNode().getLastCycleData());

                        if (CloudDriver.getInstance().getNodeManager().getNode(nodeConfig.getNodeName()).isPresent()) {
                            channel.sendPacket(PacketCloudEntityNode.forDataResponse(nodeConfig.getNodeName(), PacketCloudEntityNode.ResponsePayLoad.ALREADY_NODE_EXISTS, currentNode));
                            CloudDriver.getInstance().getLogger().error(Translation.of("node.client.node.connect.error.already.connected", nodeConfig.getNodeName()));
                            return;
                        } else if (getNodeName().equalsIgnoreCase(nodeConfig.getNodeName())) {
                            channel.sendPacket(PacketCloudEntityNode.forDataResponse(nodeConfig.getNodeName(), PacketCloudEntityNode.ResponsePayLoad.SAME_NAME_AS_HEAD_NODE, currentNode));
                            CloudDriver.getInstance().getLogger().error(Translation.of("node.client.node.connect.error.same.name", nodeConfig.getNodeName()));
                            return;
                        }

                        String authKey = nodeConfig.getAuthKey();
                        if (authKey.equals(NodeDriver.getInstance().getNode().getConfig().getAuthKey())) {
                            channel.sendPacket(PacketCloudEntityNode.forDataResponse(getNodeName(), PacketCloudEntityNode.ResponsePayLoad.SUCCESS, currentNode));

                            //right auth key -> registering node
                            UniversalNode node = new UniversalNode(nodeConfig, data);
                            node.setChannel(channel);

                            CloudDriver.getInstance()
                                    .getEventManager()
                                    .callEvent(new CloudEventNodeRegister(node), PublishingType.GLOBAL);

                            NodeDriver.getInstance().getServiceQueue().dequeue();

                        } else {
                            channel.sendPacket(PacketCloudEntityNode.forDataResponse(getNodeName(), PacketCloudEntityNode.ResponsePayLoad.WRONG_AUTH_KEY, currentNode));
                            CloudDriver.getInstance().getLogger().error(Translation.of("node.client.node.connect.error.wrong.key", nodeConfig.getNodeName()));
                        }

                    }
                });
            } else {
                Task<INode> node = CloudDriver.getInstance().getNodeManager().getNode(channel.getName());

                if (node.isNull()) {
                    return;
                }
                CloudDriver.getInstance()
                        .getEventManager()
                        .callEvent(new CloudEventNodeUnregister(node.get()), PublishingType.GLOBAL);
            }
        } else {
            if (state == ConnectionState.CONNECTED) {
                // set online
                CloudService service = CloudDriver.getInstance().getServiceManager().getCachedCloudService(channel.getName());
                if (service == null) {
                    //other remote connection

                    PacketDriverCacheUpdate.publishUpdate(channel);

                    if (channel.getName().equalsIgnoreCase("Application")) {
                        InetSocketAddress address = (InetSocketAddress) channel.context().channel().remoteAddress();
                        NodeDriver.getInstance().getLogger().info("§a==> Channel §8[§b" + channel.getName() + "@" + address.getHostName() + ":" + address.getPort() + "§8] §7connected");
                    }

                    return;
                }

                PacketDriverCacheUpdate.publishUpdate(service);
                // update cache

                service.update();

                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceConnect(service), PublishingType.GLOBAL);

            } else {
                String service = channel.getName();
                if (service.equalsIgnoreCase("Application")) {
                    NodeDriver.getInstance().getLogger().warn("§a==> Channel §e{} - {} disconnected", "Cloud Application", channel);
                    return;
                }
                NodeDriver base = NodeDriver.getInstance();
                CloudService cloudService = base.getServiceManager().getCachedCloudService(service);
                if (cloudService != null) {
                    CloudDriver.getInstance().getServiceManager().unregisterService(cloudService);
                } else {
                    NodeDriver.getInstance().getLogger().warn(Translation.of("node.client.unknown.disconnect"), channel.getName(), channel);
                }
            }

            Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                NodeDriver.getInstance().getServiceQueue().dequeue();
            }, 200L);
        }
    }

    public void registerStats(CloudService service) {
        bootUpStatistics.put(service.getName(), System.currentTimeMillis());
    }

    public long getStats(CloudService service) {
        long time = System.currentTimeMillis() - bootUpStatistics.getOrDefault(service.getName(), (System.currentTimeMillis() - 1));
        bootUpStatistics.remove(service.getName());
        return time;
    }

    @Override
    public void sendPacket(IPacket packet, String... receiver) {
        for (String s : receiver) {
            PacketChannel connectedChannel = this.getConnectedChannel(s);
            if (connectedChannel == null) {
                continue;
            }
            connectedChannel.sendPacket(packet);
        }
    }


}
