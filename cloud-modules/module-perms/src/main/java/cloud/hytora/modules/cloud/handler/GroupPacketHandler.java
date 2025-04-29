package cloud.hytora.modules.cloud.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.modules.global.packets.PermsGroupPacket;

public class GroupPacketHandler implements PacketHandler<PermsGroupPacket> {

    @Override
    public void handle(PacketChannel wrapper, PermsGroupPacket packet) {
        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);
        switch (packet.getPayLoad()) {
            case CREATE:
                permissionManager.addPermissionGroup(packet.getGroup());
                break;
            case REMOVE:
                String name = packet.getName();
                permissionManager.deletePermissionGroup(name);
                break;
            case UPDATE:
                permissionManager.updatePermissionGroup(packet.getGroup());
                break;
        }
    }
}
