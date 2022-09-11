package cloud.hytora.driver.node;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.networking.INetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.INodeCycleData;
import cloud.hytora.driver.services.ICloudServer;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * The {@link INode} represents the big Cluster-Nodes in the API
 * You can retrieve their {@link INodeConfig} or their last {@link INodeCycleData},
 * start a certain {@link ICloudServer}, stop a certain {@link ICloudServer}
 * or even shutdown the node or directly message them
 *
 * @author Lystx
 * @version SNAPSHOT-1.1
 */
public interface INode extends IClusterObject<INode>, INetworkExecutor {

    /**
     * The name of the node object using {@link #getConfig()}
     */
    @Nonnull
    default String getName() {
        return getConfig().getNodeName();
    }

    /**
     * Returns the {@link INodeConfig} of this node
     * @see INodeConfig
     */
    @Nonnull
    INodeConfig getConfig();

    /**
     * Returns the {@link INodeCycleData} of this node
     * @see INodeCycleData
     */
    @Nonnull
    INodeCycleData getLastCycleData();

    /**
     * Sets the {@link INodeCycleData} for this node
     * <b>Caution:</b> Only use if you know why you do it
     *
     * @param lastCycleData the cycle data
     */
    void setLastCycleData(@Nonnull INodeCycleData lastCycleData);

    /**
     * Returns if this node has enough memory left to
     * start a certain {@link ICloudServer}
     *
     * @param cloudServer the server to check
     */
    boolean hasEnoughMemoryToStart(@Nonnull ICloudServer cloudServer);

    /**
     * Adds up the memory used by every single {@link ICloudServer}
     * and returns that value
     */
    long getUsedMemoryByServices();

    /**
     * Shuts down this node and if head-node
     * migrates to another node
     */
    void shutdown();

    /**
     * Stops a certain {@link ICloudServer} on this {@link INode}.
     * If the {@link ICloudServer} is not running on this Node
     * nothing will happen and the request is being ignored
     *
     * @param server the server to stop
     */
    void stopServer(@Nonnull ICloudServer server);

    /**
     * Starts a certain {@link ICloudServer} on this {@link INode}.
     * If the {@link ICloudServer} is not running on this Node
     * nothing will happen and the request is being ignored
     *
     * @param server the server to start
     */
    void startServer(@Nonnull ICloudServer server);

    /**
     * Starts a certain {@link ICloudServer} on this {@link INode} async.
     * If the {@link ICloudServer} is not running on this Node
     * nothing will happen and the request is being ignored
     *
     * @param server the server to start
     * @return task instance containing the response for stopping
     */
    @Nonnull
    IPromise<NetworkResponseState> startServerAsync(@Nonnull ICloudServer server);

    /**
     * Stops a certain {@link ICloudServer} on this {@link INode} async.
     * If the {@link ICloudServer} is not running on this Node
     * nothing will happen and the request is being ignored
     *
     * @param server the server to stop
     * @return task instance containing the response for stopping
     */
    @Nonnull
    IPromise<NetworkResponseState> stopServerAsync(@Nonnull ICloudServer server);

    /**
     * Retrieves a {@link Collection} of all running {@link ICloudServer}s
     * on this {@link INode}.
     * If no servers are running the {@link Collection} will be empty.
     */
    @Nonnull
    Collection<ICloudServer> getRunningServers();

    @Nonnull
    IPromise<Collection<ICloudServer>> getRunningServersAsync();

}
