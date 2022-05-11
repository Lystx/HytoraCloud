package cloud.hytora.driver.networking.packets.node;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.NodeCycleData;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NodeCycleDataPacket extends Packet {

    private String nodeName;
    private NodeCycleData data;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                nodeName = buf.readString();
                data = buf.readObject(NodeCycleData.class);
                break;

            case WRITE:
                buf.writeString(nodeName);
                buf.writeObject(data);
                break;
        }
    }
}
