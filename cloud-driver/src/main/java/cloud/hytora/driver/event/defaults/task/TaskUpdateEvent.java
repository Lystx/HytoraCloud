package cloud.hytora.driver.event.defaults.task;

import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.task.DefaultServiceTask;
import cloud.hytora.driver.services.task.ServiceTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TaskUpdateEvent implements ProtocolTansferableEvent {

    private ServiceTask task;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        if (state == BufferState.READ) {
            task = buf.readObject(DefaultServiceTask.class);
        } else {
            buf.writeObject(task);
        }
    }
}
