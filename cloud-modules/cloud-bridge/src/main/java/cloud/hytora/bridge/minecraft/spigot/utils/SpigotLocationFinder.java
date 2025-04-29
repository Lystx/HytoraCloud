package cloud.hytora.bridge.minecraft.spigot.utils;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerLocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpigotLocationFinder implements PlayerLocationFinder {
    @Override
    public CloudEntityLocation<Double, Float> getLocation(ICloudPlayer player) {
        Player player1 = Bukkit.getPlayer(player.getName());
        if (player1 != null) {
            return new CloudEntityLocation<Double, Float>(
                    player1.getLocation().getX(),
                    player1.getLocation().getY(),
                    player1.getLocation().getZ(),
                    player1.getLocation().getYaw(),
                    player1.getLocation().getPitch(),
                    player1.getLocation().getWorld().getName()
            );
        }
        return null;
    }
}
