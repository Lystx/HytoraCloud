package cloud.hytora.node.impl.handler.packet.universal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheRegisterPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.services.ServiceManager;

public class NodeServiceAddPacketHandler implements PacketHandler<CloudServerCacheRegisterPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, CloudServerCacheRegisterPacket packet) {
        ServiceManager cloudServiceManager = CloudDriver.getInstance().getServiceManager();

        cloudServiceManager.registerService(packet.getService());
    }
}
