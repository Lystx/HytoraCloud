package cloud.hytora.node.impl.handler;

import cloud.hytora.driver.networking.packets.node.NodeRequestServerStartPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteServerStartHandler implements PacketHandler<NodeRequestServerStartPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, NodeRequestServerStartPacket packet) {
        CloudServer server = packet.getServer();
        NodeDriver.getInstance().startServer(server);
    }
}
