package cloud.hytora.driver.networking;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

/**
 * What is the difference between a {@link Cluster} and an {@link EndpointNetworkExecutor} you might ask?
 * Well the answer is not that simple.<br>
 * An {@link EndpointNetworkExecutor} is basically just the plain Netty-Server that makes it possible for
 * others to connect to, but it really isn't any more than (like the name says) an connection-endpoint.
 * Now the {@link Cluster} on the other hand, is more the final product for a CloudNode than the {@link EndpointNetworkExecutor}
 * Its more of a parent for starting {@link CloudService}s than the other thing.
 *
 * @see EndpointNetworkExecutor
 *
 * @author Lystx
 * @version STABLE-1.8
 * @since STABLE-1.6
 */
public interface Cluster extends EndpointNetworkExecutor {

    /**
     * Registers the bootup statistics for this {@link CloudService}
     *
     * @param service the service to register
     */
    void registerStats(CloudService service);

    /**
     * Gets and removes the stats of the bootup of the provided {@link CloudService}
     *
     * @param service the service to get
     * @return the time in ms as long
     */
    long getStats(CloudService service);

    /**
     * Sends an {@link IPacket} to every connected {@link PacketChannel}
     *
     * @param packet the packet to send
     * @see EndpointNetworkExecutor#getAllConnectedChannels()
     */
    void sendPacketToAll(IPacket packet);

    /**
     * Sends an {@link IPacket} to a specific receiver
     * that matches the provided name(s)
     *
     * @param packet the packet to send
     * @param receiver the name of the receiver(s)
     */
    void sendPacket(IPacket packet, String... receiver);

    /**
     * The host name of this cluster
     */
    String getHostName();

    /**
     * The port of this cluster
     */
    int getPort();
}
