package cloud.hytora.driver.node;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.services.CloudServer;

import java.util.List;

public interface Node extends NetworkExecutor {

    List<CloudServer> getRunningServers();

    NodeCycleData getLastCycleData();

    void setLastCycleData(NodeCycleData data);

    INodeConfig getConfig();

    void shutdown();

    void log(String message, Object... args);

    void stopServer(CloudServer server);

    void startServer(CloudServer server);
}
