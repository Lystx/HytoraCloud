package cloud.hytora.driver.services.task.packet;


import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.UniversalServiceTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTaskExecutePacket extends AbstractPacket {

    private IServiceTask serviceTask;
    private ExecutionPayLoad payLoad;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.serviceTask = buf.readObject(UniversalServiceTask.class);
                this.payLoad = buf.readEnum(ExecutionPayLoad.class);
                break;

            case WRITE:
                buf.writeObject(serviceTask);
                buf.writeEnum(payLoad);
                break;
        }
    }

    public enum ExecutionPayLoad {
        REMOVE, CREATE
    }

}
