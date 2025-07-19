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
 * got unregistered within the cache of the current Driver Instance
 * and is now ready to work with
 *
 * @author Lystx
 * @see CloudEventServiceRegistered
 * @see CloudEventServiceUpdate
 * @since SNAPSHOT-1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CloudEventServiceUnregistered implements NetworkEvent {

    /**
     * The name of the service that is being unregistered
     */
    private String service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {
            case READ:
                service = buf.readString();
                break;
            case WRITE:
                buf.writeString(service);
                break;
        }
    }

    public CloudService getCloudServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.service);
    }

}
