package cloud.hytora.driver.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.networking.INetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudServer;

import java.util.Collection;

// TODO: 15.08.2022 documentation 
public interface INode extends IClusterObject<INode>, INetworkExecutor {

    @Override
    default String getName() {
        return getConfig().getNodeName();
    }

    INodeConfig getConfig();

    INodeData getLastCycleData();

    void setLastCycleData(INodeData lastCycleData);

    boolean hasEnoughMemoryToStart(ICloudServer cloudServer);

    long getUsedMemoryByServices();

    void shutdown();

    void stopServer(ICloudServer server);

    Task<NetworkResponseState> stopServerAsync(ICloudServer server);

    void startServer(ICloudServer server);

    Task<NetworkResponseState> startServerAsync(ICloudServer server);

    Collection<ICloudServer> getRunningServers();

    Task<Collection<ICloudServer>> getRunningServersAsync();

}
