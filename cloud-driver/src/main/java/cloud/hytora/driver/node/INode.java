package cloud.hytora.driver.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.networking.INetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudService;

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

    boolean hasEnoughMemoryToStart(ICloudService cloudServer);

    long getUsedMemoryByServices();

    void shutdown();

    void stopServer(ICloudService server);

    Task<NetworkResponseState> stopServerAsync(ICloudService server);

    void startServer(ICloudService server);

    Task<NetworkResponseState> startServerAsync(ICloudService server);

    Collection<ICloudService> getRunningServers();

    Task<Collection<ICloudService>> getRunningServersAsync();

}
