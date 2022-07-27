package cloud.hytora.modules.impl;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultPermission implements Permission {

    private String permission;
    private long expirationDate;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                permission = buf.readString();
                expirationDate = buf.readLong();
                break;
            case WRITE:
                buf.writeString(permission);
                buf.writeLong(expirationDate);
                break;
        }
    }
    @Override
    public boolean hasExpired() {
        long currentTime = System.currentTimeMillis();
        return expirationDate != 1 && currentTime > expirationDate;
    }
}
