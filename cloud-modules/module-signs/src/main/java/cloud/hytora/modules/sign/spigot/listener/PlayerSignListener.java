package cloud.hytora.modules.sign.spigot.listener;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.spigot.BukkitCloudSignAPI;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerSignListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() == Material.AIR) {
            return;
        }
        if (!(event.getClickedBlock().getType().equals(Material.WALL_SIGN))) {
            return;
        }

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());
        ICloudSign cloudSign = ((BukkitCloudSignAPI) CloudSignAPI.getInstance()).getSignUpdater().getCloudSign(sign.getLocation());

        if (cloudPlayer != null && cloudSign != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String s = ((BukkitCloudSignAPI) CloudSignAPI.getInstance()).getSignUpdater().getServiceMap().get(cloudSign);
            if (s == null) {
                return; //no server for this sign (offline layout)
            }
            ICloudService service = CloudDriver.getInstance().getServiceManager().getCachedCloudService(s);
            if (service == null) {
                return;
            }

            PlayerExecutor.forPlayer(cloudPlayer).connect(service);
        }
    }
}