package cloud.hytora.driver.networking.packets.entities;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketProperty;

import java.util.function.Consumer;

public class PacketCloudEntityPlayerExtension extends BufferPacket {


    public static PacketCloudEntityPlayerExtension forProxy(ProxyPayLoad payLoad, Consumer<PacketBuffer> bufferConsumer) {
        return new PacketCloudEntityPlayerExtension(buf -> buf.writeEnum(Type.PROXY).writeEnum(payLoad).append(bufferConsumer));
    }

    public static PacketCloudEntityPlayerExtension forBukkit(BukkitPayLoad payLoad, Consumer<PacketBuffer> bufferConsumer) {
        return new PacketCloudEntityPlayerExtension(buf -> buf.writeEnum(Type.BUKKIT).writeEnum(payLoad).append(bufferConsumer));
    }

    public PacketCloudEntityPlayerExtension() {
    }

    public PacketCloudEntityPlayerExtension(PacketProperty... properties) {
        super(properties);
    }

    public PacketCloudEntityPlayerExtension(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }

    public enum Type {

        PROXY,

        BUKKIT
    }

    public enum ProxyPayLoad {



        PLAYER_EXECUTE_KICK,

        PLAYER_EXECUTE_MESSAGE,

        PLAYER_EXECUTE_COMPONENT_MESSAGE,

        PLAYER_EXECUTE_CONNECT,

        PLAYER_EXECUTE_TAB_LIST
    }

    public enum BukkitPayLoad {

        GET_LOCATION,

        TELEPORT_LOCATION

    }
}
