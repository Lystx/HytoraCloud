package cloud.hytora.node.handler.packet.normal;

import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.packets.defaults.RedirectPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.config.MainConfiguration;
import cloud.hytora.node.node.NodeBasedClusterExecutor;

import java.util.Optional;

public class NodeRedirectPacketHandler implements PacketHandler<RedirectPacket> {

    @Override
    public void handle(PacketChannel wrapper, RedirectPacket packet) {

        if (packet.getClient().equalsIgnoreCase(MainConfiguration.getInstance().getNodeConfig().getNodeName())) {
            NodeDriver.getInstance().getNetworkExecutor().handlePacket(wrapper, packet.getPacket()); //handle if should be redirected to this node
            return;
        }
        NodeBasedClusterExecutor executor = NodeDriver.getInstance().getNetworkExecutor();
        Optional<ClusterClientExecutor> client = executor.getClient(packet.getClient());
        client.ifPresent(s -> s.sendPacket(packet.getPacket()));
    }
}
