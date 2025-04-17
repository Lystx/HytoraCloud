package cloud.hytora.modules.cloud.handler;


import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;

public class PlayerUpdatePacketHandler implements PacketHandler<PermsPlayerUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, PermsPlayerUpdatePacket packet) {
        PermissionPlayer player = packet.getPlayer();

        player.update();
    }
}
