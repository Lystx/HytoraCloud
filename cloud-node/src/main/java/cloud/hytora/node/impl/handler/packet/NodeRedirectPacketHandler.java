package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.config.def.UniversalNetworkConfig;

public class NodeRedirectPacketHandler implements PacketHandler<PacketRedirecting> {

    @Override
    public void handle(PacketChannel channel, PacketRedirecting packet) {
        trace("Incoming PacketRedirection [supposed client={}, packet={}]", packet.getClient(), packet.getPacket().getClass().getSimpleName());
        if (packet.getClient().equalsIgnoreCase(UniversalNetworkConfig.getInstance().getNodeConfig().getNodeName())) {
            trace("  => Handling on this Node");
            NodeDriver.getInstance().getExecutor().handlePacket(channel, packet.getPacket()); //handle if should be redirected to this node
            return;
        }
        //sending to all if required
        if (packet.getClient().equalsIgnoreCase("ALL")) {
            trace("  => Redirecting packet to all and handling on this Node");
            NodeDriver.getInstance().getExecutor().handlePacket(channel, packet.getPacket()); //handle if should be redirected to this node
            NodeDriver.getInstance().getExecutor().sendPacketToAll(packet.getPacket());
            return;
        }

        trace("  => Redirecting to {}...", packet.getClient());
        Cluster cluster = NodeDriver.getInstance().getExecutor();

        PacketChannel connectedChannel = cluster.getConnectedChannel(packet.getClient());
        if (connectedChannel == null) {
            return;
        }
        connectedChannel.sendPacket(packet.getPacket());
    }
}
