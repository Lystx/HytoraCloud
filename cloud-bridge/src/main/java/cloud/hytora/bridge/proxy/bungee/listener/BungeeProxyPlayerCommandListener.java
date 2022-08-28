package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.bridge.IBridgePlugin;
import lombok.Data;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * This listener listens for Chat-Input on Proxy-Side
 * and checks if the input matches any registered Cloud-Sided-Command
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
@Data
public class BungeeProxyPlayerCommandListener implements Listener {

    /**
     * The plugin bridge instance to handle command
     */
    private final IBridgePlugin bridge;

    @EventHandler
    public void handleCommand(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) {

            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            bridge.handleCommandExecution(proxiedPlayer.getUniqueId(), event.getMessage(), event::setCancelled);
        }
    }


}
