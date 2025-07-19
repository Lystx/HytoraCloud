package cloud.hytora.modules.cloud.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.modules.global.packets.PermsPlayerRequestPacket;

import java.util.UUID;

public class PlayerPacketHandler implements PacketHandler<PermsPlayerRequestPacket> {

    @Override
    public void handle(PacketChannel channel, PermsPlayerRequestPacket packet) {
        String name = packet.getName();
        UUID uniqueId = packet.getUniqueId();


        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);
        if (name == null) {
            PermissionPlayer permissionPlayer = permissionManager.getPermissionPlayer(uniqueId);
            if (permissionPlayer == null) {
                CloudDriver.getInstance().getLogger().error("Tried to send nulled PermissionPlayer for ID " + uniqueId);
                return;
            }
            channel.sendResponse().setBuffer(buf -> buf.writeObject(permissionPlayer)).execute(packet);
        } else {
            PermissionPlayer permissionPlayer = permissionManager.getPermissionPlayer(name);
            if (permissionPlayer == null) {
                CloudDriver.getInstance().getLogger().error("Tried to send nulled PermissionPlayer for name " + name);
                return;
            }
            channel.sendResponse().setBuffer(buf -> buf.writeObject(permissionPlayer)).execute(packet);
        }
    }
}
