package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketProperty;

import java.util.function.Consumer;

public class PacketTemplate extends BufferPacket {

    public static PacketTemplate forFiles(ServiceTemplate template, String dirName) {
        return new PacketTemplate(buf -> buf.writeEnum(PayLoad.GET_FILES).writeObject(template).writeString(dirName));
    }


    public PacketTemplate() {
    }

    public PacketTemplate(PacketProperty... properties) {
        super(properties);
    }

    public PacketTemplate(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }


    public enum PayLoad {

        GET_FILES;

    }
}
