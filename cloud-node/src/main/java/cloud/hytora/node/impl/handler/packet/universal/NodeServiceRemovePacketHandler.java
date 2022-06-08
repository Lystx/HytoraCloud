package cloud.hytora.node.impl.handler.packet.universal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUnregisterPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.ServiceManager;

import java.util.Optional;

public class NodeServiceRemovePacketHandler implements PacketHandler<CloudServerCacheUnregisterPacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudServerCacheUnregisterPacket packet) {
        ServiceManager cloudServiceManager = CloudDriver.getInstance().getServiceManager();
        Optional<ServiceInfo> service = cloudServiceManager.getService(packet.getService());

        service.ifPresent(cloudServiceManager::unregisterService);
    }
}
