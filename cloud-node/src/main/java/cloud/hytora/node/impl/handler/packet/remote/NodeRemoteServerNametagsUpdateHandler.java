package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.packet.ServiceUpdateNametagsPacket;

public class NodeRemoteServerNametagsUpdateHandler implements PacketHandler<ServiceUpdateNametagsPacket> {

    @Override
    public void handle(PacketChannel wrapper, ServiceUpdateNametagsPacket packet) {
        String server = packet.getService();
        Task<ICloudService> service = CloudDriver.getInstance().getServiceManager().getCloudService(server);
        service.ifPresent(s -> s.sendPacket(packet));
    }
}
