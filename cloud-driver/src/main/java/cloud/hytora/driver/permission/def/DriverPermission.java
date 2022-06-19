package cloud.hytora.driver.permission.def;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DriverPermission implements Permission {

    /**
     * The permission value
     */
    private String permission;

    /**
     * The expiration Date in millis
     */
    private long expirationDate;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(permission);
                buf.writeLong(expirationDate);
                break;
            case READ:
                permission = buf.readOptionalString();
                expirationDate = buf.readLong();
                break;
        }
    }

    @Override
    public boolean hasExpired() {
        long currentTimeMillis = System.currentTimeMillis();

        return this.expirationDate != -1 && currentTimeMillis > this.expirationDate;
    }
}
