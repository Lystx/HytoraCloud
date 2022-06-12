package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.packets.*;
import cloud.hytora.driver.networking.packets.group.ServiceConfigurationExecutePacket;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.networking.packets.module.RemoteModuleControllerPacket;
import cloud.hytora.driver.networking.packets.module.RemoteModuleExecutionPacket;
import cloud.hytora.driver.networking.packets.node.*;
import cloud.hytora.driver.networking.packets.player.*;
import cloud.hytora.driver.networking.packets.services.*;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.ResponsePacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.SimplePacket;
import com.google.common.collect.Maps;

import lombok.Getter;

import java.util.Map;

public class PacketProvider {

    @Getter
    private static final Map<Integer, Class<? extends Packet>> registeredPackets = Maps.newConcurrentMap();

    public static int getPacketId(Class<? extends Packet> clazz) {
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
        PacketProvider.autoRegister(NodeConnectionDataRequestPacket.class);
        PacketProvider.autoRegister(NodeConnectionDataResponsePacket.class);
        PacketProvider.autoRegister(NodeCycleDataPacket.class);
        PacketProvider.autoRegister(NodeRequestShutdownPacket.class);
        PacketProvider.autoRegister(NodeRequestServerStopPacket.class);
        PacketProvider.autoRegister(NodeRequestServerStartPacket.class);

        //Service packets
        PacketProvider.autoRegister(ServiceShutdownPacket.class);
        PacketProvider.autoRegister(CloudServerCacheUnregisterPacket.class);
        PacketProvider.autoRegister(CloudServerCacheUpdatePacket.class);
        PacketProvider.autoRegister(ServiceRequestShutdownPacket.class);
        PacketProvider.autoRegister(CloudServerCommandPacket.class);

        //updating packet
        PacketProvider.autoRegister(DriverUpdatePacket.class);
        PacketProvider.autoRegister(StorageUpdatePacket.class);

        //module packets
        PacketProvider.autoRegister(RemoteModuleExecutionPacket.class);
        PacketProvider.autoRegister(RemoteModuleControllerPacket.class);

        //service group packets
        PacketProvider.autoRegister(ServiceConfigurationExecutePacket.class);
        PacketProvider.autoRegister(ServerConfigurationCacheUpdatePacket.class);

        //cloud player packets
        PacketProvider.autoRegister(CloudPlayerLoginPacket.class);
        PacketProvider.autoRegister(CloudPlayerDisconnectPacket.class);
        PacketProvider.autoRegister(CloudPlayerUpdatePacket.class);
        PacketProvider.autoRegister(CloudPlayerKickPacket.class);
        PacketProvider.autoRegister(CloudPlayerSendServicePacket.class);
        PacketProvider.autoRegister(CloudPlayerPlainMessagePacket.class);
        PacketProvider.autoRegister(CloudPlayerComponentMessagePacket.class);
        PacketProvider.autoRegister(CloudPlayerTabListPacket.class);
        PacketProvider.autoRegister(OfflinePlayerRequestPacket.class);

        //util packets
        PacketProvider.autoRegister(RedirectPacket.class);
        PacketProvider.autoRegister(ResponsePacket.class);
        PacketProvider.autoRegister(SimplePacket.class);
        PacketProvider.autoRegister(DriverLoggingPacket.class);
        PacketProvider.autoRegister(DriverCallEventPacket.class);
    }

    public static void autoRegister(Class<? extends Packet> packetClass) {
        registerPacket(packetClass, generatePacketId());
    }

    public static void registerPacket(Class<? extends Packet> packetClass, int id) {
        if (registeredPackets.containsKey(id)) {
            registerPacket(packetClass, (id + 1));
            return;
        }
        registeredPackets.put(id, packetClass);
    }

    public static int generatePacketId() {
        return registeredPackets.keySet().size() + 1;
    }

    public static Class<? extends Packet> getPacketClass(int id) {
        if (!registeredPackets.containsKey(id)) {
            return null;
        }
        return registeredPackets.get(id);
    }

}
