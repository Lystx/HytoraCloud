package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import io.netty.channel.ChannelHandlerContext;
import cloud.hytora.driver.networking.NetworkExecutor;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;

public interface ChannelWrapper {

    /**
     * If a connection has been built up before
     */
    boolean hasEverConnected();

    /**
     * Default method to check if this wrapper is connected
     * by checking if the {@link ConnectionState} equals {@link ConnectionState#CONNECTED}
     */
    default boolean isConnected() {
        return state() == ConnectionState.CONNECTED;
    }

    /**
     * Flushes a {@link Packet} into this context
     *
     * @param packet the packet to send
     */
    void flushPacket(Packet packet);

    /**
     * Closes this context
     */
    void close();

    /**
     * The wrapped netty channel context
     */
    ChannelHandlerContext context();

    /**
     * The wrapped context in an optional instance
     */
    Optional<ChannelHandlerContext> optional();

    /**
     * The state of this context
     */
    ConnectionState state();

    /**
     * The executor for this packet
     */
    NetworkExecutor executor();

    void sendPacket(Packet packet);

    /**
     * The last modification time
     */
    long modificationTime();

    /**
     * The {@link InetSocketAddress} of this context
     */
    InetSocketAddress getClientAddress();

    ChannelWrapper overrideExecutor(NetworkExecutor executor);

    ChanneledPacketAction<Set<BufferedResponse>> prepareMultiQuery();

    ChanneledPacketAction<BufferedResponse> prepareSingleQuery();

    ChanneledPacketAction<Void> prepareResponse();

    ChanneledPacketAction<Void> prepareTransfer();

}
