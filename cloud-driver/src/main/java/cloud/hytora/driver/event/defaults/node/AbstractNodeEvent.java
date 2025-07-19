package cloud.hytora.driver.event.defaults.node;

import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.UniversalNode;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public abstract class AbstractNodeEvent implements NetworkEvent {

    protected INode node;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.node = buf.readObject(UniversalNode.class);
                break;
            case WRITE:
                buf.writeObject(this.node);
                break;
        }
    }
}
