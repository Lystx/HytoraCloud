package cloud.hytora.driver.node;

import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.services.ServiceInfo;

import java.util.List;

public interface Node extends IClusterObject<Node>, NetworkExecutor {

    List<ServiceInfo> getRunningServers();

    NodeCycleData getLastCycleData();

    void setLastCycleData(NodeCycleData data);

    INodeConfig getConfig();

    void shutdown();

    void log(String message, Object... args);

    void stopServer(ServiceInfo server);

    void startServer(ServiceInfo server);
}
