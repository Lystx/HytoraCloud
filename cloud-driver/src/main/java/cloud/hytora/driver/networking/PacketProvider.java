package cloud.hytora.driver.networking;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.packets.*;
import cloud.hytora.driver.node.packet.*;
import cloud.hytora.driver.services.task.packet.ServiceTaskExecutePacket;

import cloud.hytora.driver.module.packet.RemoteModuleControllerPacket;
import cloud.hytora.driver.module.packet.RemoteModuleExecutionPacket;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.ResponsePacket;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.services.packet.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PacketProvider {

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
        PacketProvider.autoRegister(HandshakePacket.class);
        PacketProvider.autoRegister(AuthenticationPacket.class);
        PacketProvider.autoRegister(NodeConnectionDataRequestPacket.class);
        PacketProvider.autoRegister(NodeConnectionDataResponsePacket.class);
        PacketProvider.autoRegister(NodeCycleDataPacket.class);
        PacketProvider.autoRegister(NodeRequestShutdownPacket.class);
        PacketProvider.autoRegister(NodeRequestServerStopPacket.class);
        PacketProvider.autoRegister(NodeRequestServerStartPacket.class);

        //Service packets
        PacketProvider.autoRegister(ServiceForceShutdownPacket.class);
        PacketProvider.autoRegister(ServiceRequestShutdownPacket.class);
        PacketProvider.autoRegister(ServiceUpdateNametagsPacket.class);
        PacketProvider.autoRegister(ServiceConfigPacket.class);
        PacketProvider.autoRegister(ServiceCommandPacket.class);
        PacketProvider.autoRegister(ServiceStartPacket.class);

        //updating packet
        PacketProvider.autoRegister(DriverRequestCachePacket.class);
        PacketProvider.autoRegister(DriverUpdatePacket.class);
        PacketProvider.autoRegister(StorageUpdatePacket.class);

        //module packets
        PacketProvider.autoRegister(RemoteModuleExecutionPacket.class);
        PacketProvider.autoRegister(RemoteModuleControllerPacket.class);

        //service group packets
        PacketProvider.autoRegister(ServiceTaskExecutePacket.class);

        //cloud player packets
        PacketProvider.autoRegister(PacketCloudPlayer.class);
        PacketProvider.autoRegister(PacketOfflinePlayer.class);

        //util packets
        PacketProvider.autoRegister(RedirectPacket.class);
        PacketProvider.autoRegister(ResponsePacket.class);
        PacketProvider.autoRegister(DriverLoggingPacket.class);
        PacketProvider.autoRegister(ChannelMessageExecutePacket.class);
        PacketProvider.autoRegister(DriverCallEventPacket.class);
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
