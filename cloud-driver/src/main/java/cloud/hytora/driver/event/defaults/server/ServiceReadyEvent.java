package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link CloudEvent} signals that a certain {@link ICloudService}
 * is already registered and is now connected and authenticated and ready to use
 *
 * @author Lystx
 * @see ServiceUpdateEvent
 * @see ServiceUnregisterEvent
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor @Getter
@NoArgsConstructor
public class ServiceReadyEvent implements ProtocolTansferableEvent {

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

    public ICloudService getCloudServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.name);
    }

    public Task<ICloudService> getCloudServerAsync() {
        return CloudDriver.getInstance().getServiceManager().getCloudService(this.name);
    }
}
