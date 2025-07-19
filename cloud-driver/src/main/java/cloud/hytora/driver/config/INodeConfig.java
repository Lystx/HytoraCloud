package cloud.hytora.driver.config;

import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.UUID;

/**
 *The {@link INodeConfig} is the configuration for every {@link INode}.
 * It contains the name, the id, the address of this node and the authentication and so on and so on.
 *
 *
 * @see INode
 * @see INodeCycleData
 *
 * @since SNAPSHOT-1.5
 * @author Lystx
 */
public interface INodeConfig extends IBufferObject {

    /**
     * @return the name of this node
     */
    String getNodeName();

    /**
     * Sets the name of this node
     *
     * @param name the name to set
     */
    void setNodeName(String name);

    /**
     * @return the unique identifier of this node
     */
    UUID getUniqueId();

    /**
     * Sets the ({@link UUID}) unique identifier of this node
     *
     * @param uniqueId the id to set
     */
    void setUniqueId(UUID uniqueId);

    /**
     * @return the address of this node
     */
    ProtocolAddress getAddress();

    /**
     * Sets the {@link ProtocolAddress} of this node
     *
     * @param address the address to set
     */
    void setAddress(ProtocolAddress address);

    /**
     * @return the auth-key of this node
     */
    String getAuthKey();

    /**
     * Sets the {@link String} auth-key of this node
     *
     * @param authKey the authKey to set
     */
    void setAuthKey(String authKey);

    /**
     * @return the amount of services that are allowed to be started at the same time
     */
    int getMaxBootableServicesAtSameTime();

    /**
     * Sets the maxBootServices of this node
     *
     * @param amount the amount to set
     * @see #getMaxBootableServicesAtSameTime()
     */
    void setMaxBootableServicesAtSameTime(int amount);

    /**
     * @return the provided memory for this node
     */
    long getMemory();

    /**
     * Sets the provided memeory for this node
     *
     * @param memory the memory in MB
     */
    void setMemory(long memory);

    /**
     * All the {@link ProtocolAddress}es
     */
    ProtocolAddress[] getClusterAddresses();

    /**
     * Sets the {@link ProtocolAddress}es
     *
     * @param adresses the addresses
     */
    void setClusterAddresses(ProtocolAddress[] adresses);

    /**
     * @return if the provided {@link INode} is a remote or HeadNode
     */
    boolean isRemote();

    /**
     * Sets the remote-state for the provided {@link INode}
     *
     * @param b the state
     */
    void setRemote(boolean b);

    //default method .... ignore
    default void setRemote() {
        this.setRemote(true);
    }

}
