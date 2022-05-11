package cloud.hytora.node.impl.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUnregisterPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.ServiceManager;

import java.util.Optional;

public class NodeServiceRemovePacketHandler implements PacketHandler<CloudServerCacheUnregisterPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, CloudServerCacheUnregisterPacket packet) {
        ServiceManager cloudServiceManager = CloudDriver.getInstance().getServiceManager();
        Optional<CloudServer> service = cloudServiceManager.getService(packet.getService());

        service.ifPresent(cloudServiceManager::unregisterService);
    }
}
