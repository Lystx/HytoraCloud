package cloud.hytora.driver.networking.protocol;

import cloud.hytora.document.gson.adapter.ExcludeIfNull;
import cloud.hytora.driver.common.exception.HytoraCloudException;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.simplejson.api.annotation.JsonExcludeField;
import cloud.hytora.simplejson.api.annotation.JsonExcludeIfNull;
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

    /**
     * The host-address of this address
     */
    private String host;

    /**
     * the port of this address
     */
    private int port;

    /**
     * The authKey provided if used
     * as a way to connect two nodes
     * with eachOther then we will
     * pass on the authKey in the address
     */
    @ExcludeIfNull
    @JsonExcludeIfNull
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

    public static ProtocolAddress fromSocketAddress(InetSocketAddress address) {
        return new ProtocolAddress(address.getAddress().getHostAddress(), address.getPort());
    }

    public static ProtocolAddress fromString(String input) {
        if (input.contains(":")) {
            String[] data = input.split(":");
            String host = data[0];
            String portString = data[1];

            try {
                int port = Integer.parseInt(portString);

                return new ProtocolAddress(host, port);
            } catch (NumberFormatException e) {
                throw new HytoraCloudException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
            }

        } else {
            throw new HytoraCloudException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
        }
    }

    @JsonExcludeField
    private static ProtocolAddress cachedPublicIpInstance;

    public String toString() {
        return host + ":" + port;
    }
}
