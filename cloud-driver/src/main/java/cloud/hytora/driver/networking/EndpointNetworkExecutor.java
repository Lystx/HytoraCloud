package cloud.hytora.driver.networking;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * An {@link EndpointNetworkExecutor} builds upon the {@link HandlingNetworkExecutor}
 * It has all the same abilities, but here it differentiates between client and server
 * A client and server are both {@link HandlingNetworkExecutor}s because both can handle/send {@link IPacket}s
 * But what differentiates the server from the client?
 * The following methods in this class are only available for the server, because they manage
 * all the connected clients as {@link PacketChannel}s
 *
 * @author Lystx
 * @version STABLE-2.0
 * @see PacketChannel
 * @see IPacket
 * @see HandlingNetworkExecutor
 * @since SNAPSHOT-1.5
 */
public interface EndpointNetworkExecutor extends HandlingNetworkExecutor {

    /**
     * Opens the server for other connections to bind to
     *
     * @param hostname the hostname of this endpoint
     * @param port     the port of this endpoint
     * @return the task that contains the result of this method
     * @see Task
     */
    Task<EndpointNetworkExecutor> openConnection(String hostname, int port);

    /**
     * Shuts down the current endpoint-server and stops
     * the possibility for others to connect to this server
     *
     * @return the task that contains the result of this method
     * @see Task
     */
    Task<Boolean> shutdown();

    /**
     * This method sends an {@link IPacket} to a specific {@link NetworkComponent}
     * that has bound to this network executor
     *
     * @param packet    the packet to send
     * @param component the receiver
     */
    void sendPacket(IPacket packet, NetworkComponent component);

    /**
     * Tries to get a connected {@link PacketChannel}
     * by its provided netty {@link Channel} instance
     *
     * @param channel the channel to match
     * @return found instance or null
     */
    @Nullable
    PacketChannel getConnectedChannel(Channel channel);

    /**
     * Tries to get a connected {@link PacketChannel}
     * by its provided name
     *
     * @param name the name to match
     * @return found instance or null
     */
    @Nullable
    PacketChannel getConnectedChannel(String name);

    /**
     * Tries to get a connected {@link PacketChannel}
     * by its provided {@link UUID}
     *
     * @param uniqueId the uniqueId to match
     * @return found instance or null
     */
    @Nullable
    PacketChannel getConnectedChannel(UUID uniqueId);

    /**
     * @return all connected {@link PacketChannel}s
     * @see PacketChannel
     */
    @NotNull
    Collection<PacketChannel> getAllConnectedChannels();

    /**
     * @return all connected {@link PacketChannel}s that match
     * the provided {@link ConnectionType}
     *
     * @param type the type to match
     * @see PacketChannel
     */
    @NotNull
    Collection<PacketChannel> getConnectedChannels(ConnectionType type);

}
