package cloud.hytora.driver.services.fallback;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface FallbackEntry extends Bufferable {

    boolean isEnabled();
    void setEnabled(boolean enabled);

    String getPermission();
    void setPermission(String permission);

    int getPriority();
    void setPriority(int priority);

    @Override
    default void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

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
