package cloud.hytora.node.handler.packet.remote;

import cloud.hytora.driver.node.packet.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteShutdownHandler implements PacketHandler<NodeRequestShutdownPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestShutdownPacket packet) {
        if (packet.getName().equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
            NodeDriver.getInstance().getNode().shutdown();
        }
    }
}
