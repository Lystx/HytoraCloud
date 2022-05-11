package cloud.hytora.driver.networking.protocol;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@Getter
@Setter
public class ProtocolAddress implements Bufferable {

    private String host;
    private int port;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                host = buf.readString();
                port = buf.readInt();
                break;

            case WRITE:
                buf.writeString(host);
                buf.writeInt(port);
                break;
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
