package cloud.hytora.driver.networking.packets.entities;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class PacketCloudEntityService extends AbstractPacket {


    public static PacketCloudEntityService forCommandPacket(String command) {
        return new PacketCloudEntityService(PayLoad.COMMAND, buf -> buf.writeString(command));
    }


    public static PacketCloudEntityService forConfiguration(UUID uniqueId, String serviceTask, int port, int memory, int maxPlayers, boolean ignoreOfLimit, String motd, String node, Document properties, Collection<ServiceTemplate> templates, ServiceVersion version) {
        return new PacketCloudEntityService(PayLoad.CONFIG, buf -> {


            buf.writeUniqueId(uniqueId);
            buf.writeString(serviceTask);
            buf.writeInt(port);
            buf.writeInt(memory);
            buf.writeInt(maxPlayers);
            buf.writeBoolean(ignoreOfLimit);
            buf.writeString(motd);
            buf.writeString(node);
            buf.writeDocument(properties);
            buf.writeObjectCollection(templates);
            buf.writeEnum(version);
        });
    }

    public static PacketCloudEntityService forForceShutdown(CloudService cloudService) {
        return new PacketCloudEntityService(PayLoad.FORCE_SHUTDOWN, buf -> buf.writeString(cloudService.getName()));
    }

    public static PacketCloudEntityService forRequestShutdown(CloudService cloudService) {
        return new PacketCloudEntityService(PayLoad.REQUEST_SHUTDOWN, buf -> buf.writeString(cloudService.getName()));
    }

    public static PacketCloudEntityService forUpdateNameTags(CloudService cloudService) {
        return new PacketCloudEntityService(PayLoad.UPDATE_NAMETAGS, buf -> buf.writeString(cloudService.getName()));
    }

    public static PacketCloudEntityService forStart(CloudService cloudService) {
        return new PacketCloudEntityService(PayLoad.START, buf -> buf.writeService(cloudService));
    }

    public static PacketCloudEntityService forUpdate(CloudService cloudService) {
        return new PacketCloudEntityService(PayLoad.UPDATE_SERVER, buf -> buf.writeService(cloudService));
    }

    @Getter
    @Setter
    private PayLoad payLoad;

    public PacketCloudEntityService() {
    }

    public PacketCloudEntityService(PayLoad payLoad, Consumer<PacketBuffer> buffer) {
        super(buffer);
        this.payLoad = payLoad;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.payLoad = buf.readEnum(PayLoad.class);
                break;
            case WRITE:
                buf.writeEnum(payLoad);
                break;
        }
    }



    public enum PayLoad {

        COMMAND,

        CONFIG,

        FORCE_SHUTDOWN,

        REQUEST_SHUTDOWN,

        UPDATE_SERVER,

        START,

        UPDATE_NAMETAGS

    }
}
