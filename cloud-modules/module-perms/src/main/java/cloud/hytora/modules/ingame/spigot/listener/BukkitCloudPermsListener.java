package cloud.hytora.modules.ingame.spigot.listener;

import cloud.hytora.modules.ingame.spigot.BukkitCloudPermsHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BukkitCloudPermsListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(PlayerLoginEvent event) {
        BukkitCloudPermsHelper.injectPermissible(event.getPlayer());
        if (event.getPlayer().hasPermission("*")) {
            event.getPlayer().setOp(true);
        }
    }
}
