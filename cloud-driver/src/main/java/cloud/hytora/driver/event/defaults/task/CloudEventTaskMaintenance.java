package cloud.hytora.driver.event.defaults.task;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@Getter
public class CloudEventTaskMaintenance implements LocalEvent, NetworkEvent {

    private ServiceTask task;
    private boolean newMaintenanceValue;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {

        switch (state) {

            case READ:
                task = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(buffer.readString());
                this.newMaintenanceValue = buffer.readBoolean();
                break;

            case WRITE:
                buffer.writeString(task.getName());
                buffer.writeBoolean(this.newMaintenanceValue);
                break;
        }
    }
}
