package cloud.hytora.bridge.minecraft.spigot.handler;

import cloud.hytora.bridge.minecraft.spigot.utils.Nametag;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.packet.ServiceUpdateNametagsPacket;
import cloud.hytora.remote.Remote;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpigotNametagHandler implements PacketHandler<ServiceUpdateNametagsPacket> {

    @Override
    public void handle(PacketChannel wrapper, ServiceUpdateNametagsPacket packet) {

        if (packet.getService().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            Nametag nametag = new Nametag();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                nametag.updateNameTags(onlinePlayer);
            }
        }
    }
}
