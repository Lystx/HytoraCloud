package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ServiceCrashPrevention implements IBufferObject {

    private boolean enabled;
    private long time;
    private TimeUnit timeUnit;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeBoolean(enabled);
                buf.writeLong(time);
                buf.writeEnum(timeUnit);
                break;
            case READ:
                enabled = buf.readBoolean();
                time = buf.readLong();
                timeUnit = buf.readEnum(TimeUnit.class);
                break;
        }
    }
}
