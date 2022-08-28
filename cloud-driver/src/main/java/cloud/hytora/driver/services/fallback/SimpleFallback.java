package cloud.hytora.driver.services.fallback;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleFallback implements ICloudFallback {

    private boolean enabled;
    private String permission;
    private int priority;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                setEnabled(buf.readBoolean());
                setPermission(buf.readString());
                setPriority(buf.readInt());
                break;

            case WRITE:
                buf.writeBoolean(isEnabled());
                buf.writeString(getPermission());
                buf.writeInt(getPriority());
                break;
        }
    }

}
