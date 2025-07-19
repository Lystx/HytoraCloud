package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeLoggingPacketHandler implements PacketHandler<PacketDriverLogging> {

    @Override
    public void handle(PacketChannel channel, PacketDriverLogging packet) {
        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.getName().equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
            NodeDriver.getInstance().getLogger().info(message);
        } else {
            PacketChannel client = NodeDriver.getInstance().getExecutor().getConnectedChannel(component.getName());
            if (client == null) {
                return;
            }
            client.sendPacket(packet);
        }
    }
}
