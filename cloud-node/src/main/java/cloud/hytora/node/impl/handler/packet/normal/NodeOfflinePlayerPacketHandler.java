package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.packet.PacketOfflinePlayer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.node.NodeDriver;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NodeOfflinePlayerPacketHandler implements PacketHandler<PacketOfflinePlayer> {


    @Override
    public void handle(PacketChannel wrapper, PacketOfflinePlayer packet) {
        PacketBuffer buffer = packet.buffer();
        PacketOfflinePlayer.PayLoad payLoad = buffer.readEnum(PacketOfflinePlayer.PayLoad.class);

        if (payLoad == PacketOfflinePlayer.PayLoad.SAVE_PLAYER) {

            //saving player on this node side
            DefaultCloudOfflinePlayer player = buffer.readObject(DefaultCloudOfflinePlayer.class);
            NodeDriver.getInstance().getPlayerManager().saveOfflinePlayer(player);
            return;
        }

        if (payLoad == PacketOfflinePlayer.PayLoad.GET_ALL) {
            wrapper.prepareResponse().buffer(buf -> buf.writeObjectCollection(NodeDriver.getInstance().getPlayerManager().getOfflinePlayers().timeOut(TimeUnit.SECONDS, 10).syncUninterruptedly().orElse(new ArrayList<>()))).execute(packet);
            return;
        }

        if (payLoad == PacketOfflinePlayer.PayLoad.GET_BY_NAME) {
            String name = buffer.readString();
            CloudOfflinePlayer player = NodeDriver.getInstance().getPlayerManager().getOfflinePlayer(name).syncUninterruptedly().orElse(null);

            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(player)).execute(packet);
            return;
        }
        if (payLoad == PacketOfflinePlayer.PayLoad.GET_BY_UUID) {
            UUID uuid = buffer.readUniqueId();
            CloudOfflinePlayer player = NodeDriver.getInstance().getPlayerManager().getOfflinePlayer(uuid).syncUninterruptedly().orElse(null);

            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(player)).execute(packet);
        }
    }
}
