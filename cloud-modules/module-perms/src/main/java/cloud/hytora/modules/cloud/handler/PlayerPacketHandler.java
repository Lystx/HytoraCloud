package cloud.hytora.modules.cloud.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.modules.global.packets.PermsPlayerRequestPacket;

import java.util.UUID;

public class PlayerPacketHandler implements PacketHandler<PermsPlayerRequestPacket> {

    @Override
    public void handle(PacketChannel wrapper, PermsPlayerRequestPacket packet) {
        String name = packet.getName();
        UUID uniqueId = packet.getUniqueId();


        PermissionManager permissionManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);

        System.out.println("RECEIVED REQUEST FOR PERMS PLAYER");
        if (name == null) {
            System.out.println("==> BY UUID");
            permissionManager.getPlayerAsyncByUniqueId(uniqueId).onTaskSucess(player -> {
               wrapper.prepareResponse().buffer(buf -> buf.writeObject(player)).execute(packet);
            });
        } else {
            System.out.println("==> BY NAME");
            permissionManager.getPlayerAsyncByName(name).onTaskSucess(player -> {
                wrapper.prepareResponse().buffer(buf -> buf.writeObject(player)).execute(packet);
            });
        }
    }
}
