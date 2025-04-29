package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.LoggingDriver;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

public interface PacketHandler<T extends IPacket> extends LoggingDriver {

    void handle(PacketChannel wrapper, T packet);
}
