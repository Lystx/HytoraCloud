package cloud.hytora.driver.networking.protocol;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetSocketAddress;

@AllArgsConstructor
@Getter
@Setter
public class ProtocolAddress implements Bufferable {

    private String host;
    private int port;
    private String authKey;

    public ProtocolAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ProtocolAddress(@Nonnull InetSocketAddress socketAddress) {
        this.host = socketAddress.getAddress().getHostAddress();
        this.port = socketAddress.getPort();
    }

    public ProtocolAddress(@Nonnull InetSocketAddress socketAddress, String authKey) {
        this.host = socketAddress.getAddress().getHostAddress();
        this.port = socketAddress.getPort();
        this.authKey = authKey;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                host = buf.readString();
                port = buf.readInt();
                authKey = buf.readOptionalString();
                break;

            case WRITE:
                buf.writeString(host);
                buf.writeInt(port);
                buf.writeOptionalString(authKey);
                break;
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
