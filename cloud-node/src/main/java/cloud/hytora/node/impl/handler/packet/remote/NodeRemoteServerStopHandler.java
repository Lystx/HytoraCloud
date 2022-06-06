package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.node.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.node.NodeDriver;

import java.util.Optional;

public class NodeRemoteServerStopHandler implements PacketHandler<NodeRequestServerStopPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStopPacket packet) {
        String server = packet.getServerName();
        Optional<CloudServer> service = CloudDriver.getInstance().getServiceManager().getService(server);
        service.ifPresent(NodeDriver.getInstance()::stopServer);
    }
}
