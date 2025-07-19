package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.common.DriverUtility;
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
 * got updated within the cache of the current Driver Instance
 *
 * @author Lystx
 * @see CloudEventServiceRegistered
 * @see CloudEventServiceUnregistered
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudEventServiceUpdate extends DriverUtility implements NetworkEvent {

    /**
     * The server that got unregistered
     */
    private CloudService service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeService(service);
                break;

            case READ:
                service = buf.readService();
                break;
        }
    }

    @Override
    public String toString() {
        return args("ServiceUpdateEvent[name={}, visibility={}, state={}]", service.getName(), service.getServiceVisibility(), service.getServiceState());
    }
}
