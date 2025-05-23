package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.node.NodeBasedClusterExecutor;

import java.util.Optional;

public class NodeRedirectPacketHandler implements PacketHandler<RedirectPacket> {

    @Override
    public void handle(PacketChannel wrapper, RedirectPacket packet) {

        if (packet.getClient().equalsIgnoreCase(MainConfiguration.getInstance().getNodeConfig().getNodeName())) {
            NodeDriver.getInstance().getExecutor().handlePacket(wrapper, packet.getPacket()); //handle if should be redirected to this node
            return;
        }
        NodeBasedClusterExecutor executor = NodeDriver.getInstance().getExecutor();
        Optional<ClusterClientExecutor> client = executor.getClient(packet.getClient());
        client.ifPresent(s -> s.sendPacket(packet.getPacket()));
    }
}
