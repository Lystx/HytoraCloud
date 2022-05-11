package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.IPacket;

public interface EndpointNetworkExecutor extends AdvancedNetworkExecutor {

    void sendPacket(IPacket packet, NetworkComponent component);
}
