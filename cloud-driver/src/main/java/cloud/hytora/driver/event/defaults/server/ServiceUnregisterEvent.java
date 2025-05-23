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
 * got unregistered within the cache of the current Driver Instance
 * and is now ready to work with
 *
 * @author Lystx
 * @see ServiceRegisterEvent
 * @see ServiceUpdateEvent
 * @since SNAPSHOT-1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceUnregisterEvent implements ProtocolTansferableEvent {

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

    public ICloudService getCloudServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.service);
    }

    public Task<ICloudService> getCloudServerAsync() {
        return CloudDriver.getInstance().getServiceManager().getCloudService(this.service);
    }
}
