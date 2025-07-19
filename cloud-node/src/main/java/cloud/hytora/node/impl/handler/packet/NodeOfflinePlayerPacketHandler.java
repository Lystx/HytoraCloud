package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityOfflinePlayer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.node.NodeDriver;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NodeOfflinePlayerPacketHandler implements PacketHandler<PacketCloudEntityOfflinePlayer> {


    @Override
    public void handle(PacketChannel channel, PacketCloudEntityOfflinePlayer packet) {
        PacketBuffer buffer = packet.buffer();
        PacketCloudEntityOfflinePlayer.PayLoad payLoad = buffer.readEnum(PacketCloudEntityOfflinePlayer.PayLoad.class);

        if (payLoad == PacketCloudEntityOfflinePlayer.PayLoad.SAVE_PLAYER) {

            //saving player on this node side
            DefaultCloudOfflinePlayer player = buffer.readObject(DefaultCloudOfflinePlayer.class);
            NodeDriver.getInstance().getPlayerManager().saveOfflinePlayer(player);
            return;
        }

        if (payLoad == PacketCloudEntityOfflinePlayer.PayLoad.GET_ALL) {
            channel.sendResponse().setBuffer(buf -> buf.writeObjectCollection(NodeDriver.getInstance().getPlayerManager().getOfflinePlayers().timeOut(TimeUnit.SECONDS, 10).syncUninterruptedly().orElse(new ArrayList<>()))).execute(packet);
            return;
        }

        if (payLoad == PacketCloudEntityOfflinePlayer.PayLoad.GET_BY_NAME) {
            String name = buffer.readString();
            CloudOfflinePlayer player = NodeDriver.getInstance().getPlayerManager().getOfflinePlayer(name).syncUninterruptedly().orElse(null);

            channel.sendResponse().setBuffer(buf -> buf.writeOptionalObject(player)).execute(packet);
            return;
        }
        if (payLoad == PacketCloudEntityOfflinePlayer.PayLoad.GET_BY_UUID) {
            UUID uuid = buffer.readUniqueId();
            CloudOfflinePlayer player = NodeDriver.getInstance().getPlayerManager().getOfflinePlayer(uuid).syncUninterruptedly().orElse(null);

            channel.sendResponse().setBuffer(buf -> buf.writeOptionalObject(player)).execute(packet);
        }
    }
}
