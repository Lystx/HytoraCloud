package cloud.hytora.driver.uuid.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

@Getter
@NoArgsConstructor
public class CachedUUIDPacket extends AbstractPacket {

    public CachedUUIDPacket(PayLoad payLoad, Consumer<PacketBuffer> bufferConsumer) {
        super(buf -> buf.writeEnum(payLoad));
        buffer().append(bufferConsumer);
    }


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

    }

    public enum PayLoad {

        LOAD_CACHE,

        UPDATE_CACHE

    }
}
