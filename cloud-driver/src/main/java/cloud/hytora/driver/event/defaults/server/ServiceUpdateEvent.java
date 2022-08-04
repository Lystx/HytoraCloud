package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This {@link CloudEvent} signals that a certain {@link ICloudServer}
 * got updated within the cache of the current Driver Instance
 *
 * @author Lystx
 * @see ServiceRegisterEvent
 * @see ServiceUnregisterEvent
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ServiceUpdateEvent extends DriverUtility implements ProtocolTansferableEvent {

    /**
     * The server that got unregistered
     */
    private ICloudServer service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeObject(service);
                break;

            case READ:
                service = buf.readObject(UniversalCloudServer.class);
                break;
        }
    }

    @Override
    public String toString() {
        return args("ServiceUpdateEvent[name={}, visibility={}, state={}]", service.getName(), service.getServiceVisibility(), service.getServiceState());
    }
}
