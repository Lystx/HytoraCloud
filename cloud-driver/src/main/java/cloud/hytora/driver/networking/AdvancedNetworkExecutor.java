package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface AdvancedNetworkExecutor extends NetworkExecutor {

    <T extends Packet> void handlePacket(@Nullable PacketChannel wrapper, @Nonnull T packet);

    <T extends Packet> void registerChannelHandler(@Nonnull String channelName, @Nonnull PacketHandler<T> packetHandler);

    <T extends Packet> void unRegisterChannelHandler(@Nonnull String channelName, @Nonnull PacketHandler<T> packetHandler);

    void unregisterChannelHandlers(@Nonnull String channelName);

    <T extends Packet> void registerPacketHandler(@Nonnull PacketHandler<T> packetHandler);

    <T extends Packet> void registerSelfDestructivePacketHandler(@Nonnull PacketHandler<T> packetHandler);

    PacketChannel getPacketChannel();

    @Nonnull
    List<PacketHandler<?>> getRegisteredPacketHandlers();
}
