package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;

public interface IPacketExecutor {

    void sendPacket(IPacket packet);

}
