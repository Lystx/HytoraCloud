package cloud.hytora.driver.networking.packets.entities;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferPacket;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCloudEntityNode extends AbstractPacket {


    public static PacketCloudEntityNode forDataRequest() {
        return new PacketCloudEntityNode(PayLoad.DATA_REQUEST);
    }

    public static PacketCloudEntityNode forFilesRequest() {
        return new PacketCloudEntityNode(PayLoad.REQUEST_FILES, buf -> buf.writeString("none"));
    }

    public static PacketCloudEntityNode forFileRequest(String fileName) {
        return new PacketCloudEntityNode(PayLoad.REQUEST_FILES, buf -> buf.writeString(fileName));
    }

    public static PacketCloudEntityNode forDataResponse(String node, ResponsePayLoad payLoad, INode nodeInfo) {
        return new PacketCloudEntityNode(PayLoad.DATA_RESPONSE, buf -> buf.writeString(node).writeEnum(payLoad).writeObject(nodeInfo));
    }

    public static PacketCloudEntityNode forServerStart(CloudService service, boolean demandsResponse) {
        return new PacketCloudEntityNode(PayLoad.SERVER_START, buf -> buf.writeService(service).writeBoolean(demandsResponse));
    }

    public static PacketCloudEntityNode forServerStop(CloudService service, boolean demandsResponse) {
        return new PacketCloudEntityNode(PayLoad.SERVER_STOP, buf -> buf.writeString(service.getName()).writeBoolean(demandsResponse));
    }

    public static PacketCloudEntityNode forNodeShutdown(INode node) {
        return new PacketCloudEntityNode(PayLoad.NODE_SHUTDOWN, buf -> buf.writeString(node.getName()));
    }

    public static PacketCloudEntityNode forCycleData(INode node, INodeCycleData data) {
        return new PacketCloudEntityNode(PayLoad.CYCLE_DATA, buf -> buf.writeString(node.getConfig().getNodeName()).writeObject(data));
    }

    @Getter @Setter
    private PayLoad payLoad;

    public PacketCloudEntityNode(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }

    public PacketCloudEntityNode(PayLoad payLoad, Consumer<PacketBuffer> buffer) {
        super(buffer);
        this.payLoad = payLoad;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeOptionalEnum(payLoad);
                break;
            case READ:
                payLoad = buf.readOptionalEnum(PayLoad.class);
        }
    }


    public enum PayLoad {

        REQUEST_FILES,

        DATA_REQUEST,

        DATA_RESPONSE,

        CYCLE_DATA,

        SERVER_START,

        SERVER_STOP,

        NODE_SHUTDOWN,

    }

    public enum ResponsePayLoad {

        SUCCESS,

        WRONG_AUTH_KEY,

        ALREADY_NODE_EXISTS,

        SAME_NAME_AS_HEAD_NODE

    }

}
