package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.packet.ServiceRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ServiceManager;

public class NodeServiceShutdownHandler implements PacketHandler<ServiceRequestShutdownPacket> {

    @Override
    public void handle(PacketChannel wrapper, ServiceRequestShutdownPacket packet) {
        String serverName = packet.getService();
        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        ICloudServer service = serviceManager.getServiceByNameOrNull(serverName);

        if (service != null) {
            service.shutdown();
        }
    }
}
