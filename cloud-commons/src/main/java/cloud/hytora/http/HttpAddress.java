package cloud.hytora.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * The {@link HttpAddress} describes an easy-to-use address for internal Cloud-API-Use
 * It contains the host, and port (as normal addresses like {@link InetSocketAddress} also do)
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
@Getter
@Setter
public class HttpAddress {


    /**
     * The hos that this address belongs to
     */
    private String host;

    /**
     * The port of this address
     */
    private int port;

    /**
     * Constructs a new {@link HttpAddress} without an authKey
     *
     * @param host the host of this address
     * @param port the port of this address
     */
    public HttpAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Constructs a new {@link HttpAddress} by getting values
     * from an already existing {@link InetSocketAddress}
     *
     * @param socketAddress the address
     */
    public HttpAddress(@Nonnull InetSocketAddress socketAddress) {
        this.host = socketAddress.getAddress().getHostAddress();
        this.port = socketAddress.getPort();
    }


    public static HttpAddress fromString(String input) throws RuntimeException {
        if (input.contains(":")) {
            String[] data = input.split(":");
            String host = data[0];
            String portString = data[1];

            try {
                int port = Integer.parseInt(portString);

                return new HttpAddress(host, port);
            } catch (NumberFormatException e) {
                throw new RuntimeException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
            }

        } else {
            throw new RuntimeException("ProtocolAddress needs to be formatted after scheme \"host:port\"!");
        }
    }

    private static HttpAddress cachedPublicIpInstance;

    public static HttpAddress currentPublicIp() {
        if (cachedPublicIpInstance != null) {
            return cachedPublicIpInstance;
        }
        try {
            String s = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com").openConnection().getInputStream())).readLine();
            cachedPublicIpInstance = new HttpAddress(s, 0);
            return cachedPublicIpInstance;
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpAddress("127.0.0.1", -1);
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
