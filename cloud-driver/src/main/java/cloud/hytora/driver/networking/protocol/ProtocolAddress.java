package cloud.hytora.driver.networking.protocol;

import cloud.hytora.document.gson.adapter.ExcludeIfNull;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.http.HttpAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * The {@link ProtocolAddress} describes an easy-to-use address for internal Cloud-API-Use
 * It contains the host, and port (as normal addresses like {@link InetSocketAddress} also do)
 * and it also contains the option to provide an authKey to authenticate certain objects within the cluster
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
@AllArgsConstructor @Getter @Setter
public class ProtocolAddress implements IBufferObject {

    /**
     * The hos that this address belongs to
     */
    private String host;

    /**
     * The port of this address
     */
    private int port;

    /**
     * The authKey (will not be put into any json-context)
     * that might help to authenticate certain objects
     */
    @ExcludeIfNull
    private String authKey;

    /**
     * Constructs a new {@link ProtocolAddress} without an authKey
     *
     * @param host the host of this address
     * @param port the port of this address
     */
    public ProtocolAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Constructs a new {@link ProtocolAddress} by getting values
     * from an already existing {@link InetSocketAddress}
     *
     * @param socketAddress the address
     */
    public ProtocolAddress(@Nonnull InetSocketAddress socketAddress) {
        this.host = socketAddress.getAddress().getHostAddress();
        this.port = socketAddress.getPort();
    }

    /**
     * Constructs a new {@link ProtocolAddress} by using an already
     * existing {@link InetSocketAddress} and also appends an authKey
     *
     * @param socketAddress the address
     * @param authKey the key to authenticate
     */
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

    public HttpAddress toHttp() {
        return new HttpAddress(host, port);
    }

    public String toString() {
        return host + ":" + port;
    }
}
