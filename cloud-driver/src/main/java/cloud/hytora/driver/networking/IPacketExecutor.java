package cloud.hytora.driver.networking;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.protocol.packets.IPacket;

// TODO: 21.08.2022 documentation
public interface IPacketExecutor {

    void sendPacket(IPacket packet);

    Task<Void> sendPacketAsync(IPacket packet);

}
