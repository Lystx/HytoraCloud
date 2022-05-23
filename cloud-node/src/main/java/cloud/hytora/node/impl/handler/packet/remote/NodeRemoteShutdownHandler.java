package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.driver.networking.packets.node.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteShutdownHandler implements PacketHandler<NodeRequestShutdownPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, NodeRequestShutdownPacket packet) {
        if (packet.getName().equalsIgnoreCase(NodeDriver.getInstance().getName())) {
            NodeDriver.getInstance().shutdown();
        }
    }
}
