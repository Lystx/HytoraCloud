package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link CloudEvent} signals that a certain {@link ICloudService}
 * got registered within the cache of the current Driver Instance
 * and is now ready to work with
 *
 * @author Lystx
 * @see ServiceUpdateEvent
 * @see ServiceUnregisterEvent
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor @Getter
@NoArgsConstructor
public class ServiceRegisterEvent implements ProtocolTansferableEvent {

    /**
     * The server that is being registered
     */
    private ICloudService cloudServer;

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
