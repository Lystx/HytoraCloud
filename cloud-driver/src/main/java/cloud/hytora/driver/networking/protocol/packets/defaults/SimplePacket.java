package cloud.hytora.driver.networking.protocol.packets.defaults;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class SimplePacket extends Packet {

    public SimplePacket() {
        super();
    }

    public SimplePacket(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }

    public SimplePacket(Document document) {
        this();
        this.transferInfo.setDocument(document);
    }

    @Override
    public final void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {}

}
