package cloud.hytora.driver.networking;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.networking.protocol.packets.IPacket;

// TODO: 21.08.2022 documentation
public interface IPacketExecutor {

    void sendPacket(IPacket packet);

    IPromise<Void> sendPacketAsync(IPacket packet);

}
