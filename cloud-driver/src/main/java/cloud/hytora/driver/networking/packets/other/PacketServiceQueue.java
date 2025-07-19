package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketProperty;

import java.util.function.Consumer;

public class PacketServiceQueue extends BufferPacket {


    public static PacketServiceQueue forType(PayLoad payLoad) {
        return new PacketServiceQueue(buf -> buf.writeEnum(payLoad));
    }

    public static PacketServiceQueue forType(PayLoad payLoad, Consumer<PacketBuffer> buffer) {
        return new PacketServiceQueue(buf -> buf.writeEnum(payLoad).append(buffer));
    }

    public PacketServiceQueue() {
    }

    public PacketServiceQueue(PacketProperty... properties) {
        super(properties);
    }

    public PacketServiceQueue(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }


    public enum PayLoad {

        QUEUE,

        DEQUEUE,

        SCP_ADD_GROUP,

        SCP_REMOVE_GROUP,

        SCP_GET_PAUSED_GROUPS,

    }
}
