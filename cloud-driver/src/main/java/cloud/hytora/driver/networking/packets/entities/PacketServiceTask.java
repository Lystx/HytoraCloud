package cloud.hytora.driver.networking.packets.entities;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.UniversalServiceTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketServiceTask extends AbstractPacket {

    private ServiceTask serviceTask;
    private ExecutionPayLoad payLoad;


    public static PacketServiceTask deployFile(CloudService task, String file, ServiceTemplate template, String dest) {
        PacketServiceTask packet = new PacketServiceTask(CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream().findFirst().get(), ExecutionPayLoad.DEPLOY_FILE);
        packet.buffer.writeString(task.getName());
        packet.buffer.writeString(dest);
        packet.buffer.writeObject(template);
        packet.buffer.writeString(file);
        return packet;
    }


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
        REMOVE, CREATE, DEPLOY_FILE,
    }

}
