package cloud.hytora.driver.event.defaults.driver;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CloudEventDriverAlmostOutOfMemory implements NetworkEvent {


    /**
     * The name of the process
     * that is running out of memory
     */
    private String driverName;

    private float cpuUsage;

    private long freeMemory;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:

                this.driverName = buf.readString();
                this.cpuUsage = buf.readFloat();
                this.freeMemory = buf.readLong();
                break;
            case WRITE:
                buf.writeString(driverName);
                buf.writeFloat(cpuUsage);
                buf.writeLong(freeMemory);
                break;
        }
    }
}
