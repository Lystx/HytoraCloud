package cloud.hytora.driver.networking.packets;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.auth.*;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheRequest;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.packets.entities.*;
import cloud.hytora.driver.networking.packets.other.*;
import cloud.hytora.driver.networking.query.QueryPacket;
import cloud.hytora.driver.networking.query.QueryResponsePacket;

import cloud.hytora.driver.networking.packets.module.PacketModuleController;
import cloud.hytora.driver.networking.packets.module.PacketModuleExecute;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.auth.PacketHandshake;
import cloud.hytora.driver.networking.packets.response.PacketResponse;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    @Getter
    private static final Map<Integer, Class<? extends IPacket>> registeredPackets = new HashMap<>();

    public static int getPacketId(Class<? extends IPacket> clazz) {
        return registeredPackets.keySet().stream().filter(id -> registeredPackets.get(id).equals(clazz)).findAny().orElse(-1);

    }

    private static boolean REGISTERED_INTERNALLY = false;

    public static void registerPackets() {
        if (REGISTERED_INTERNALLY) {
            return;
        }
        REGISTERED_INTERNALLY = true;

        //registering packets...
        PacketRegistry.autoRegister(PacketHandshake.class);
        PacketRegistry.autoRegister(PacketAuthentication.class);
        PacketRegistry.autoRegister(QueryPacket.class);
        PacketRegistry.autoRegister(QueryResponsePacket.class);
        PacketRegistry.autoRegister(PacketNetworkConfig.class);
        PacketRegistry.autoRegister(PacketServiceQueue.class);
        PacketRegistry.autoRegister(PacketTemplate.class);

        //entity packets using enum payLoad
        PacketRegistry.autoRegister(PacketCloudEntityNode.class);
        PacketRegistry.autoRegister(PacketCloudEntityService.class);
        PacketRegistry.autoRegister(PacketCloudEntityPlayer.class);
        PacketRegistry.autoRegister(PacketCloudEntityPlayerExtension.class);
        PacketRegistry.autoRegister(PacketCloudEntityOfflinePlayer.class);

        //updating packet
        PacketRegistry.autoRegister(PacketDriverCacheRequest.class);
        PacketRegistry.autoRegister(PacketDriverCacheUpdate.class);

        //module packets
        PacketRegistry.autoRegister(PacketModuleExecute.class);
        PacketRegistry.autoRegister(PacketModuleController.class);

        //service group packets
        PacketRegistry.autoRegister(PacketServiceTask.class);

        //util packets
        PacketRegistry.autoRegister(PacketRedirecting.class);
        PacketRegistry.autoRegister(PacketResponse.class);
        PacketRegistry.autoRegister(PacketDriverLogging.class);
        PacketRegistry.autoRegister(PacketGenericChannelMessage.class);
        PacketRegistry.autoRegister(PacketCallEvent.class);
    }

    public static void autoRegister(Class<? extends AbstractPacket> packetClass) {
        registerPacket(packetClass, generatePacketId());
    }

    public static void registerPacket(Class<? extends AbstractPacket> packetClass, int id) {
        if (registeredPackets.containsKey(id)) {
            registerPacket(packetClass, (id + 1));
            return;
        }
        registeredPackets.put(id, packetClass);
        CloudDriver.getInstance().getLogger().debug("Registered Packet {} under ID {}", packetClass, id);
    }

    public static int generatePacketId() {
        return registeredPackets.keySet().size() + 1;
    }

    public static Class<? extends IPacket> getPacketClass(int id) {
        if (!registeredPackets.containsKey(id)) {
            return null;
        }
        return registeredPackets.get(id);
    }

}
