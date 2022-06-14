package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link CloudEvent} signals that a certain {@link ServiceInfo}
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
    private ServiceInfo serviceInfo;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                serviceInfo = buf.readObject(SimpleServiceInfo.class);
                break;
            case WRITE:
                buf.writeObject(serviceInfo);
                break;
        }
    }
}
