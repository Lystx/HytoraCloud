package cloud.hytora.driver.property;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProtocolPropertyObject extends AbstractPropertyObject implements IBufferObject {

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeDocument(this.properties);
                break;
            case READ:
                this.properties = buf.readDocument();
                break;
        }
    }
}
