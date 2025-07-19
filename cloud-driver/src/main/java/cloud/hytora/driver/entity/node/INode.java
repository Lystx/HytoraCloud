package cloud.hytora.driver.entity.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.common.objects.NetworkEntity;
import cloud.hytora.driver.entity.CloudEntity;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.config.INodeConfig;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import java.util.Collection;

/**
 * The {@link INode} is the heart of the whole CloudSystem and HytoraCloud.
 * The {@link INode} is used to start and stop {@link CloudService}s
 *
 *
 * @see INodeCycleData
 * @see INodeConfig
 *
 * @since SNAPSHOT-1.0
 * @author Lystx
 */
public interface INode extends NetworkEntity<INode>, NetworkExecutor, CloudEntity {

    /**
     * Override method to identify the name of this node
     *
     * @return the name of this node using the config
     */
    @Override
    default String getName() {
        return getConfig().getNodeName();
    }

    /**
     * @return the {@link INodeConfig} of this node
     * @see INodeConfig
     */
    INodeConfig getConfig();

    /**
     * Tries to retrieve the {@link PacketChannel} of this {@link INode}
     * This method can only be executed from HeadNode
     *
     * @return the channel that bound to this node instance
     * @throws IncompatibleDriverEnvironmentException if executed on any other instance than HeadNode
     */
    @Deprecated
    PacketChannel getChannel() throws IncompatibleDriverEnvironmentException;

    /**
     * @return the last {@link INodeCycleData}
     * @see INodeCycleData
     */
    INodeCycleData getLastCycleData();

    /**
     * Sets the last received {@link INodeCycleData} for this node
     *
     * @param lastCycleData the data to set
     * @see INodeCycleData
     */
    void setLastCycleData(INodeCycleData lastCycleData);

    /**
     * Checks if this node has enough memory to start provided {@link CloudService}
     *
     * @param cloudServer the server to check
     * @return either true or false
     */
    boolean hasEnoughMemoryToStart(CloudService cloudServer);

    /**
     * @return the used memory (in MB) by all {@link CloudService}s on this {@link INode}
     */
    long getUsedMemoryByServices();

    /**
     * Shuts down this {@link INode}
     */
    void shutdown();

    /**
     * Stops a {@link CloudService} on this node without awaiting a response
     *
     * @param server the server to stop
     */
    void stopServer(CloudService server);

    /**
     * Stops a {@link CloudService} async and is waiting for response
     *
     * @param server the server to stop
     * @return the task with the response-state
     */
    Task<NetworkResponseState> stopServerAsync(CloudService server);

    /**
     * Start a {@link CloudService} on this node without awaiting a response
     *
     * @param server the server to start
     */
    void startServer(CloudService server);

    /**
     * Starts a {@link CloudService} async and is waiting for response
     *
     * @param server the server to start
     * @return the task with the response-state
     */
    Task<NetworkResponseState> startServerAsync(CloudService server);

    /**
     * @return all currently running {@link CloudService} on this {@link INode}
     *
     * that means that every {@link CloudService} registered to this {@link INode}
     * has to have the {@link ServiceState#ONLINE} to be listed here
     */
    Collection<CloudService> getRunningServers();

    /**
     * @return all currently registered {@link CloudService} on this {@link INode}
     */
    Collection<CloudService> getAssignedServers();

    /**
     * Updates the node and all its data all over the network
     * inside the whole cluster system (services & nodes)
     *
     * @param publishingTypes the type it will be updated to
     */
    void update(PublishingType... publishingTypes);
}
