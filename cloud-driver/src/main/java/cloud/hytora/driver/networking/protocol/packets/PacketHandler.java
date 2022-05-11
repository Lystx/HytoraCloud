package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;

public interface PacketHandler<T> {

    void handle(ChannelWrapper wrapper, T packet);
}
