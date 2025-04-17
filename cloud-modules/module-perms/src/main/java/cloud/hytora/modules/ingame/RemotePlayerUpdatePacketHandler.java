package cloud.hytora.modules.ingame;

import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemotePlayerUpdatePacketHandler implements PacketHandler<PermsPlayerUpdatePacket> {

    private final RemotePermissionManager permissionManager;

    @Override
    public void handle(PacketChannel wrapper, PermsPlayerUpdatePacket packet) {
        PermissionPlayer player = packet.getPlayer();
        permissionManager.addToCache(player);
    }
}
