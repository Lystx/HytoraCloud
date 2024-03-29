package cloud.hytora.node.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.packet.OfflinePlayerRequestPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;

import java.util.UUID;

public class NodeOfflinePlayerPacketHandler implements PacketHandler<OfflinePlayerRequestPacket> {


    @Override
    public void handle(PacketChannel wrapper, OfflinePlayerRequestPacket packet) {
        PacketBuffer buffer = packet.buffer();
        OfflinePlayerRequestPacket.PayLoad payLoad = buffer.readEnum(OfflinePlayerRequestPacket.PayLoad.class);

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.SAVE_PLAYER) {

            //saving player on this node side
            DefaultCloudOfflinePlayer player = buffer.readObject(DefaultCloudOfflinePlayer.class);
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).saveOfflinePlayerAsync(player);
            return;
        }

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_ALL) {
            wrapper.prepareResponse().buffer(buf -> buf.writeObjectCollection(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllOfflinePlayersBlockingOrEmpty())).execute(packet);
            return;
        }

        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_BY_NAME) {
            String name = buffer.readString();
            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getOfflinePlayerByNameBlockingOrNull(name))).execute(packet);
            return;
        }
        if (payLoad == OfflinePlayerRequestPacket.PayLoad.GET_BY_UUID) {
            UUID uuid = buffer.readUniqueId();
            wrapper.prepareResponse().buffer(buf -> buf.writeOptionalObject(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getOfflinePlayerByUniqueIdBlockingOrNull(uuid))).execute(packet);
        }
    }
}
