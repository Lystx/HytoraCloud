package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.Packet;

public interface EndpointNetworkExecutor extends AdvancedNetworkExecutor {

    void sendPacket(Packet packet, NetworkComponent component);
}
