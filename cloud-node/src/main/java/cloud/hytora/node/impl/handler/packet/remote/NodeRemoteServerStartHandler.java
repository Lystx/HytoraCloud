package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.driver.networking.packets.node.NodeRequestServerStartPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteServerStartHandler implements PacketHandler<NodeRequestServerStartPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStartPacket packet) {
        ICloudServer server = packet.getServer();
        NodeDriver.getInstance().getNode().startServer(server);
    }
}
