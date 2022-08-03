package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.node.NodeDriver;

import java.util.Optional;

public class NodeRemoteServerStopHandler implements PacketHandler<NodeRequestServerStopPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStopPacket packet) {
        String server = packet.getServerName();
        Optional<ICloudServer> service = CloudDriver.getInstance().getServiceManager().getService(server);
        service.ifPresent(s -> NodeDriver.getInstance().getNode().stopServer(s));
        if (packet.isDemandsResponse()) {
            wrapper.prepareResponse().state(service.isPresent() ? NetworkResponseState.OK : NetworkResponseState.FAILED).execute(packet);
        }
    }
}
