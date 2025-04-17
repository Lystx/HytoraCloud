package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteServerStopHandler implements PacketHandler<NodeRequestServerStopPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStopPacket packet) {
        String server = packet.getServerName();
        Task<ICloudService> service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNullAsync(server);
        service.ifPresent(s -> NodeDriver.getInstance().getNode().stopServer(s));
        if (packet.isDemandsResponse()) {
            wrapper.prepareResponse().state(service.isPresent() ? NetworkResponseState.OK : NetworkResponseState.FAILED).execute(packet);
        }
    }
}
