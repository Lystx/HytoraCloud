package cloud.hytora.node.handler.packet.remote;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverLoggingPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteLoggingHandler implements PacketHandler<DriverLoggingPacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverLoggingPacket packet) {
        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.getName().equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
            NodeDriver.getInstance().getLogger().info(message);
        }
    }
}