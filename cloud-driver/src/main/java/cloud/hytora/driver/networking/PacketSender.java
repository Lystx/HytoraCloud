package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.Packet;

public interface PacketSender {

    void sendPacket(Packet packet);

}
