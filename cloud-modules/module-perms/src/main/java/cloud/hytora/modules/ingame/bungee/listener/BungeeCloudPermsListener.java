package cloud.hytora.modules.ingame.bungee.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeCloudPermsListener implements Listener {

    @EventHandler
    public void onPermsCheck(PermissionCheckEvent event) {

        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer)event.getSender();
            PermissionPlayer permissionPlayer = CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionPlayer(player.getUniqueId());
            if (permissionPlayer == null) {
                System.out.println("ERROR");
                return;
            }
            if (permissionPlayer.hasPermission("*")) {
                event.setHasPermission(true);
                return;
            }
            event.setHasPermission(permissionPlayer.hasPermission(event.getPermission()));
        }
    }
}
