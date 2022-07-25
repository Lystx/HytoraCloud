package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;

import java.util.Optional;

public interface EndpointNetworkExecutor extends AdvancedNetworkExecutor {

    void sendPacket(Packet packet, NetworkComponent component);

    int getProxyStartPort();

    int getSpigotStartPort();
    Optional<ClusterClientExecutor> getClient(String name);
}
