package cloud.hytora.driver.config.def;

import cloud.hytora.driver.config.IServiceCrashPrevention;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UniversalSCP implements IServiceCrashPrevention {

    private boolean enabled;
    private long time;
    private TimeUnit unit;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeBoolean(enabled);
                buf.writeLong(time);
                buf.writeEnum(unit);
                break;
            case READ:
                enabled = buf.readBoolean();
                time = buf.readLong();
                unit = buf.readEnum(TimeUnit.class);
                break;
        }
    }
}
