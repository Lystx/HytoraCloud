package cloud.hytora.bridge.bukkit.listener;

import cloud.hytora.bridge.IBridgePlugin;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * This listener listens for Chat-Input on Spigot-Side
 * and checks if the input matches any registered Cloud-Sided-Command
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
@Data
public class BukkitPlayerCommandListener implements Listener {

    /**
     * The plugin bridge instance to handle command
     */
    private final IBridgePlugin bridge;

    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/")) {
            return;
        }

        Player player = event.getPlayer();
        bridge.handleCommandExecution(player.getUniqueId(), event.getMessage(), event::setCancelled);
    }

}
