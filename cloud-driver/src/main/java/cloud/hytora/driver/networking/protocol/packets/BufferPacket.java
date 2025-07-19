package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A {@link BufferPacket} is not read/written the classical way.
 * It does not have fields that will be read/written to/from a {@link PacketBuffer}
 * Mostly it will write its data directly in the {@link BufferPacket#BufferPacket(Consumer)} constructor
 * and will only read it when it is being handled using {@link IPacket#buffer()}
 *
 * @since SNAPSHOT-1.5
 * @version STABLE-2.0
 * @author Lystx
 */
@NoArgsConstructor
public abstract class BufferPacket extends AbstractPacket {

    public BufferPacket(PacketProperty... properties) {
        super(properties);
    }

    public BufferPacket(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }

    @Override
    public final void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
    }
}
