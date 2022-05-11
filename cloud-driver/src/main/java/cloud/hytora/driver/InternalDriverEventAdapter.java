package cloud.hytora.driver;

import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServiceGroupUpdateEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheRegisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUpdateEvent;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheRegisterPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUnregisterPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

public class InternalDriverEventAdapter {

    public InternalDriverEventAdapter(EventManager eventManager, AdvancedNetworkExecutor executor) {
        executor.registerPacketHandler((PacketHandler<CloudServerCacheRegisterPacket>) (wrapper, packet) -> eventManager.callEvent(new CloudServerCacheRegisterEvent(packet.getService())));
        executor.registerPacketHandler((PacketHandler<CloudServerCacheUnregisterPacket>) (wrapper, packet) -> eventManager.callEvent(new CloudServerCacheUnregisterEvent(packet.getService())));
        executor.registerPacketHandler((PacketHandler<CloudServerCacheUpdatePacket>) (wrapper, packet) -> eventManager.callEvent(new CloudServerCacheUpdateEvent(packet.getService())));
        executor.registerPacketHandler((PacketHandler<ServerConfigurationCacheUpdatePacket>) (wrapper, packet) -> eventManager.callEvent(new CloudServiceGroupUpdateEvent(packet.getConfiguration())));
    }

}
