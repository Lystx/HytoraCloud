package de.lystx.hytoracloud.driver.connection.protocol.netty.client.data;

import de.lystx.hytoracloud.driver.connection.protocol.netty.global.INetworkConnection;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.api.channel.INetworkChannel;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.identification.ConnectionType;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.IPacket;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;


public interface INettyClient {

    /**
     * The name of the client
     */
    String getUsername();

    /**
     * The host of the client (e.g.: localhost)
     */
    String getHost();

    /**
     * The port of the client (e.g.: 4314) (NETTY)
     */
    int getPort();

    /**
     * The type of the client
     */
    ConnectionType getType();

    /**
     * The netty {@link Channel}
     */
    INetworkChannel getChannel();

    /**
     * Sets the channel of this client
     *
     * @param channel the channel
     */
    void setChannel(INetworkChannel channel);

    /**
     * Returns the host and port of the client as {@link InetSocketAddress} object
     *
     * @return The object mentioned above
     */
    InetSocketAddress getAddress();

    void sendPacket(INetworkConnection connection, IPacket packet);
}
