package cloud.hytora.bridge.minecraft;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.location.impl.CloudLocation;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@AllArgsConstructor
public class BukkitCloudPlayer implements CloudBukkitPlayer {

    private final CloudPlayer cloudPlayer;

    @Override
    public void sendMessage(String message) {
        player(player -> player.sendMessage(ComponentColor.translateAlternateColorCodes('&', message)));
    }

    @Override
    public CloudLocation getLocation() {
        return get(player -> new CloudLocation(
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch(),
                player.getLocation().getWorld().getName()
        ), new CloudLocation(0D, 0D, 0D, "unknown"));
    }

    @Override
    public void teleport(CloudLocation location) {
        player(player -> {
            Location bukkitLocation = new Location(
                    Bukkit.getWorld(location.getWorld()),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );
            player.teleport(bukkitLocation);
        });
    }

    <T> T get(BiSupplier<Player, T> supply, T defValue) {
        Player player = Bukkit.getPlayer(this.cloudPlayer.getUniqueId());
        if (player == null) {
            return defValue;
        }
        return supply.supply(player);
    }

    void player(Consumer<Player> handler) {
        Player player = Bukkit.getPlayer(this.cloudPlayer.getUniqueId());
        if (player == null) {
            return;
        }
        handler.accept(player);
    }
}
