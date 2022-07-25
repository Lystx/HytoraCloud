package cloud.hytora.driver.networking.packets.services;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.services.task.DefaultServiceTask;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ServiceConfigPacket extends Packet {


    private ServiceTask serviceTask;

    private int port;
    private int memory;

    private int maxPlayers;

    private boolean ignoreOfLimit;

    private String motd;

    private String node;

    private Document properties;

    private Collection<ServiceTemplate> templates;

    private ServiceVersion version;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        if (state == BufferState.READ) {
            serviceTask = buf.readObject(DefaultServiceTask.class);
            port = buf.readInt();
            memory = buf.readInt();
            maxPlayers = buf.readInt();
            ignoreOfLimit = buf.readBoolean();
            motd = buf.readString();
            node = buf.readString();
            properties = buf.readDocument();
            templates = buf.readWrapperObjectCollection(CloudTemplate.class);
            version = buf.readEnum(ServiceVersion.class);
        } else {
            buf.writeObject(serviceTask);
            buf.writeInt(port);
            buf.writeInt(memory);
            buf.writeInt(maxPlayers);
            buf.writeBoolean(ignoreOfLimit);
            buf.writeString(motd);
            buf.writeString(node);
            buf.writeDocument(properties);
            buf.writeObjectCollection(templates);
            buf.writeEnum(version);
        }
    }
}
