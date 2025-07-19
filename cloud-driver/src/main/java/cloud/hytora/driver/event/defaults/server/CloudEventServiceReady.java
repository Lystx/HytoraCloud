package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.CloudService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link LocalEvent} signals that a certain {@link CloudService}
 * is already registered and is now connected and authenticated and ready to use
 *
 * @author Lystx
 * @see CloudEventServiceUpdate
 * @see CloudEventServiceUnregistered
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor @Getter
@NoArgsConstructor
public class CloudEventServiceReady implements NetworkEvent {

    /**
     * The server that is now ready
     */
    private String name;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                name = buf.readString();
                break;
            case WRITE:
                buf.writeString(name);
                break;
        }
    }

    public CloudService getCloudServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.name);
    }

}
