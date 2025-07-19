package cloud.hytora.driver.event.defaults.server;

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
 * has connected to the cluster but is not ready to use yet
 *
 * @author Lystx
 * @see CloudEventServiceUpdate
 * @see CloudEventServiceUnregistered
 * @since SNAPSHOT-1.0
 */
@AllArgsConstructor @Getter
@NoArgsConstructor
public class CloudEventServiceConnect implements NetworkEvent {

    /**
     * The server that has connected
     */
    private String cloudService;

    public CloudEventServiceConnect(CloudService cloudService) {
        this.cloudService = cloudService.getName();
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                cloudService = buf.readString();
                break;
            case WRITE:
                buf.writeString(cloudService);
                break;
        }
    }

    public CloudService getCloudService() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(cloudService);
    }
}
