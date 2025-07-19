package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link LocalEvent} signals that a certain {@link CloudService}
 * got registered within the cache of the current Driver Instance
 * and is now ready to work with
 *
 * @author Lystx
 * @see CloudEventServiceUpdate
 * @see CloudEventServiceUnregistered
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor @Getter
@NoArgsConstructor
public class CloudEventServiceRegistered implements NetworkEvent {

    /**
     * The server that is being registered
     */
    private CloudService cloudServer;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                cloudServer = buf.readObject(UniversalCloudServer.class);
                break;
            case WRITE:
                buf.writeObject(cloudServer);
                break;
        }
    }
}
