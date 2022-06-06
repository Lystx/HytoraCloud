package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

public interface PacketHandler<T> {

    void handle(PacketChannel wrapper, T packet);
}
