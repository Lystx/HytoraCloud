package cloud.hytora.driver.node.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.node.data.DefaultNodeData;
import cloud.hytora.driver.node.data.INodeData;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NodeCycleDataPacket extends AbstractPacket {

    private String nodeName;
    private INodeData data;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                nodeName = buf.readString();
                data = buf.readObject(DefaultNodeData.class);
                break;

            case WRITE:
                buf.writeString(nodeName);
                buf.writeObject(data);
                break;
        }
    }
}
