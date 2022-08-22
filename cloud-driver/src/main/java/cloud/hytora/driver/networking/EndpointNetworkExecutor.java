package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.packets.IPacket;

import java.util.Optional;

public interface EndpointNetworkExecutor extends IHandlerNetworkExecutor {

    void sendPacket(IPacket packet, NetworkComponent component);

    int getProxyStartPort();

    int getSpigotStartPort();
    Optional<ClusterClientExecutor> getClient(String name);
}
