package cloud.hytora.node.impl.message;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.DefaultChannelMessenger;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.node.NodeBasedClusterExecutor;

import java.util.Optional;

public class NodeChannelMessenger extends DefaultChannelMessenger {

    public NodeChannelMessenger(AdvancedNetworkExecutor executor) {
        super(executor);
    }

    @Override
    public void sendChannelMessage(ChannelMessage message, NetworkComponent[] receiver) {
        NodeBasedClusterExecutor executor = NodeDriver.getInstance().getExecutor();
        AbstractPacket packet = new ChannelMessageExecutePacket(message);

        if (receiver.length > 0) {
            for (NetworkComponent messageReceiver : receiver) {
                if (messageReceiver == null) {
                    continue;
                }
                Optional<ClusterClientExecutor> client = executor.getClient(messageReceiver.getName());
                client.ifPresent(clusterClientExecutor -> clusterClientExecutor.sendPacket(packet));
            }
        } else {
            executor.sendPacketToAll(packet);
        }
    }
}
