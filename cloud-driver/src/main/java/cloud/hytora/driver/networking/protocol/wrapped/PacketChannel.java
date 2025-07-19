package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.identification.ImmutableNameHolder;
import cloud.hytora.common.identification.ModifiableNameHolder;
import cloud.hytora.common.identification.ModifiableUUIDHolder;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.UniqueReturnValue;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.types.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import io.netty.channel.ChannelHandlerContext;
import cloud.hytora.driver.networking.NetworkExecutor;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * A {@link PacketChannel} is used in many situations.<br>
 * It acts as a bridge between server and client.<br>
 * If you are sending an {@link IPacket} on the client-side it will be through this channel.<br>
 * If the server sends an {@link IPacket} to a client it will be through this channel.<br>
 * Think of it like this:<br>
 *
 * <p> {@link Cluster} => {@link PacketChannel} => {@link NetworkExecutor}</p>
 * <p> {@link NetworkExecutor} => {@link PacketChannel} => {@link Cluster}</p><br>
 *
 * Every {@link PacketChannel} is the networking-gate for any possible {@link ConnectionType}.
 * A {@link PacketChannel} can be owned by a {@link CloudService} or an {@link INode} for example.
 *
 * @author Lystx
 * @since STABLE-1.0
 * @version STABLE-2.0
 */
public interface PacketChannel extends NetworkExecutor, ModifiableNameHolder, ModifiableUUIDHolder {

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
     * If the channel has been authenticated (handshake etc)
     * Note: this only works on the node-side
     *
     * @return if channel is authenticated
     * @throws IncompatibleDriverEnvironmentException if not executed on node side
     */
    boolean isAuthenticated() throws IncompatibleDriverEnvironmentException;

    /**
     * Sets the authentication state
     *
     * @param state the state
     * @see #isAuthenticated()
     */
    void setAuthenticated(boolean state);

    /**
     * Sets the {@link ConnectionType} of this channel
     *
     * @param type the type to set
     * @see ConnectionType
     */
    void setType(ConnectionType type);

    /**
     * If this channel is writable to
     * (wrapper method of Netty)
     */
    boolean isWritable();

    /**
     * If this channel is active
     * (wrapper method of Netty)
     */
    boolean isActive();

    /**
     * Flushes a {@link IPacket} into this context
     *
     * @param packet the packet to send
     */
    void flushPacket(IPacket packet);

    /**
     * Sends an {@link IPacket} into this channel
     * but it will be executed synchronously
     * from the main-netty-thread
     *
     * @param packet the packet to send
     */
    void sendPacketSync(@Nonnull IPacket packet);

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

    /**
     * The last modification time
     */
    long modificationTime();

    /**
     * The {@link InetSocketAddress} of this context
     */
    InetSocketAddress getClientAddress();

    /**
     * Overrides the {@link NetworkExecutor} of this channel
     * for the next action to perform
     *
     * @param executor the executor to use
     * @return current channel instance
     */
    PacketChannel overrideExecutor(NetworkExecutor executor);

    /**
     * Prepares a {@link PacketAction} for a query
     * that demands a return of {@link BufferedResponse}
     *
     * @return the created action
     * @see PacketAction
     * @see BufferedResponse
     */
    @Nonnull
    PacketAction<BufferedResponse> sendQuery();

    /**
     * Prepares a {@link PacketAction} for a query
     * that demands a return of {@link BufferedResponse}
     * Note: this query will be triggered using the
     * following provided {@link IPacket}
     *
     * @param packet the packet to send
     *
     * @return the created action
     * @see IPacket
     * @see PacketAction
     * @see BufferedResponse
     */
    @Nonnull
    PacketAction<BufferedResponse> sendQuery(IPacket packet);

    /**
     * Prepares a {@link PacketAction} for a response
     *
     * @return the created action
     * @see PacketAction
     */
    @Nonnull
    PacketAction<Void> sendResponse();

    /**
     * Prepares a {@link PacketAction} for a response
     * Note: this response will be triggered using the
     * following provided {@link IPacket}
     *
     * @param packet the packet to send
     *
     * @return the created action
     * @see IPacket
     * @see PacketAction
     */
    @Nonnull
    PacketAction<Void> sendResponse(IPacket packet);

    /**
     * Creates a query of the provided {@link IPacket}
     * that demands the return of a {@link BufferedResponse}
     *
     * @param packet the packet that triggers the query
     * @return the task for async usage
     *
     * @see IPacket
     * @see BufferedResponse
     */
    @Nonnull
    Task<BufferedResponse> sendPacketQueryAsync(@Nonnull IPacket packet);

    /**
     * Creates a query of the provided {@link IPacket}
     * that demands the return of a {@link BufferedResponse}
     *
     * @return the returned response
     * @see BufferedResponse
     */
    @Nullable
    @CheckReturnValue
    @UniqueReturnValue
    BufferedResponse sendPacketQuery(@Nonnull IPacket packet);

    /**
     * Tries to find a possible {@link CloudService}
     * that might belong to this {@link PacketChannel} by
     * filtering and comparing the name of this channel
     * and the name of the provided {@link CloudService}
     *
     * @return found instance or null
     */
    default CloudService getPossibleServer() {
        return CloudDriver.getInstance()
                .getServiceManager()
                .getAllCachedServices()
                .stream()
                .filter(s -> s.getName().equalsIgnoreCase(executor().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tries to find a possible {@link INode}
     * that might belong to this {@link PacketChannel} by
     * filtering and comparing the name of this channel
     * and the name of the provided {@link INode}
     *
     * @return found instance or null
     */
    default INode getPossibleNode() {
        return CloudDriver.getInstance()
                .getNodeManager()
                .getAllCachedNodes()
                .stream()
                .filter(node -> node.getName().equalsIgnoreCase(executor().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Default method to retrieve any {@link ImmutableNameHolder} out
     * of this {@link PacketChannel}
     *
     * @return found instance or null
     *
     * @see #getPossibleNode()
     * @see #getPossibleServer()
     */
    default ImmutableNameHolder getPossibleNameHolder() {
        return getPossibleServer() == null ? getPossibleNode() : getPossibleServer();
    }


}
