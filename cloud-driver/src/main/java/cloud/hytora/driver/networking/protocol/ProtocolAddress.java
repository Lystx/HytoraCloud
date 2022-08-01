package cloud.hytora.driver.networking.protocol;

import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

@AllArgsConstructor
@Getter
@Setter
public class ProtocolAddress implements IBufferObject {

    private String host;
    private int port;

    @ExcludeJsonField
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

    public static ProtocolAddress fromString(String input) throws Exception {
        if (input.contains(":")) {
            String[] data = input.split(":");
            String host = data[0];
            String portString = data[1];

            try {
                int port = Integer.parseInt(portString);

                return new ProtocolAddress(host, port);
            } catch (NumberFormatException e) {
                throw new CloudException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
            }

        } else {
            throw new CloudException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
        }
    }

    private static ProtocolAddress cachedPublicIpInstance;

    public static ProtocolAddress currentPublicIp() {
        if (cachedPublicIpInstance != null) {
            return cachedPublicIpInstance;
        }
        try {
            cachedPublicIpInstance = fromString(new BufferedReader(new InputStreamReader(new java.net.URL("https://checkip.amazonaws.com").openConnection().getInputStream())).readLine());
            return cachedPublicIpInstance;
        } catch (Exception e) {
            return null;
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
