package cloud.hytora.driver.node.config;

import cloud.hytora.http.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.node.INode;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * The {@link INodeConfig} holds all the configuration values that
 * are necessary for an {@link INode} to exist at runtime.
 *
 * @author Lystx
 * @version SNAPSHOT-1.1
 */
public interface INodeConfig extends IBufferObject {

    /**
     * Returns the specified name for this node
     */
    @Nonnull
    String getNodeName();

    /**
     * Returns the randomly generated {@link UUID} for this node
     */
    @Nonnull
    UUID getUniqueId();

    /**
     * Returns the {@link ProtocolAddress} that this node binds to
     */
    @Nonnull
    ProtocolAddress getAddress();

    /**
     * Returns the authkey for this node to use
     * when logging in into the cluster
     */
    @Nonnull
    String getAuthKey();

    /**
     * Returns the maximum number of services that
     * are allowed to start at the same time
     */
    int getMaxBootableServicesAtSameTime();

    /**
     * Returns the maximum number of memory in MB that
     * the Node is allowed to use
     */
    long getMemory();

    /**
     * Returns the {@link ProtocolAddress}(es) that this node connects to
     */
    @Nonnull
    ProtocolAddress[] getClusterAddresses();

    /**
     * Returns if the {@link INode} this config belongs to
     * is a slave in the Cluster-System
     */
    boolean isRemote();

    /**
     * Marks this node as a slave
     * @see #isRemote
     */
    void setRemote();

}
