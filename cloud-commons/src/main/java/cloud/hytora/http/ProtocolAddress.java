package cloud.hytora.http;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.document.gson.adapter.ExcludeIfNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
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
public class ProtocolAddress {

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

    public static ProtocolAddress fromString(String input) throws Exception {
        if (input.contains(":")) {
            String[] data = input.split(":");
            String host = data[0];
            String portString = data[1];

            try {
                int port = Integer.parseInt(portString);

                return new ProtocolAddress(host, port);
            } catch (NumberFormatException e) {
                WrappedException.throwWrapped(new NumberFormatException("ProtocolAddress needs to be formatted after scheme \"host:port\"!"));
            }

        } else {
            WrappedException.throwWrapped(new NumberFormatException("ProtocolAddress needs to be formatted after scheme \"host:port\"!"));
        }
        return null;
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
