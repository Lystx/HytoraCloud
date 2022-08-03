package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.player.packet.OfflinePlayerRequestPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.node.NodeDriver;

import java.util.UUID;

public class NodeOfflinePlayerPacketHandler implements PacketHandler<OfflinePlayerRequestPacket> {


    @Override
    public void handle(PacketChannel wrapper, OfflinePlayerRequestPacket packet) {
        PacketBuffer buffer = packet.buffer();
        OfflinePlayerRequestPacket.PayLoad payLoad = buffer.readEnum(OfflinePlayerRequestPacket.PayLoad.class);

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.SAVE_PLAYER) {

            //saving player on this node side
            DefaultCloudOfflinePlayer player = buffer.readObject(DefaultCloudOfflinePlayer.class);
            NodeDriver.getInstance().getPlayerManager().saveOfflinePlayerAsync(player);
            return;
        }

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_ALL) {
            wrapper.prepareResponse().buffer(buf -> buf.writeObjectCollection(NodeDriver.getInstance().getPlayerManager().getAllOfflinePlayersBlockingOrEmpty())).execute(packet);
            return;
        }

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_BY_NAME) {
            String name = buffer.readString();
            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(NodeDriver.getInstance().getPlayerManager().getOfflinePlayerByNameBlockingOrNull(name))).execute(packet);
            return;
        }
        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_BY_UUID) {
            UUID uuid = buffer.readUniqueId();
            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(NodeDriver.getInstance().getPlayerManager().getOfflinePlayerByUniqueIdBlockingOrNull(uuid))).execute(packet);
        }
    }
}
