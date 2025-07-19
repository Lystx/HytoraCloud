package cloud.hytora.bridge.minecraft.spigot.handler;

import cloud.hytora.bridge.minecraft.spigot.utils.Nametag;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.remote.Remote;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpigotNametagHandler implements PacketHandler<PacketCloudEntityService> {

    private final Nametag nametag;

    public SpigotNametagHandler() {
        this.nametag = new Nametag();
    }

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityService packet) {
        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case UPDATE_NAMETAGS:
                if (CloudDriver.getInstance().getEntry(PermissionManager.class).isPresent()) {
                    String server = buffer.readString();
                    if (!server.equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
                        return;
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        this.nametag.updateNameTags(onlinePlayer);
                    }
                    break;
                }
        }
    }
}
