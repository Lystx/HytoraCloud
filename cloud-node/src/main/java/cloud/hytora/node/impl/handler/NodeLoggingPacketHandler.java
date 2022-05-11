package cloud.hytora.node.impl.handler;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.node.NodeDriver;

import java.util.Optional;

public class NodeLoggingPacketHandler implements PacketHandler<DriverLoggingPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, DriverLoggingPacket packet) {
        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.matches(NodeDriver.getInstance().getExecutor())) {
            NodeDriver.getInstance().getLogger().info(message);
        } else {
            Optional<ClusterClientExecutor> client = NodeDriver.getInstance().getExecutor().getClient(component.getName());
            client.ifPresent(c -> c.sendPacket(packet));
        }
    }
}
