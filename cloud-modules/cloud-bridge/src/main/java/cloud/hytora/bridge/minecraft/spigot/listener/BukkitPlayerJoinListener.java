package cloud.hytora.bridge.minecraft.spigot.listener;

import cloud.hytora.bridge.minecraft.spigot.SpigotBootstrap;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.LoginCheckResult;
import cloud.hytora.remote.adapter.RemoteAdapter;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class BukkitPlayerJoinListener implements Listener {

    private final SpigotBootstrap spigot;

    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        spigot.startStopTimer();
    }

    @EventHandler
    public void handleLogin(PlayerLoginEvent event) {

        CloudService service = CloudDriver.getInstance().getServiceManager().thisService();
        Player player = event.getPlayer();
        CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getName());

        if (cloudPlayer == null) {
            event.setKickMessage("Please only join via a ProxyServer!");
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }
        //Player has joined; server is not stopping
        if (spigot.getShutdownSchedulerId() != -1) {
            CloudDriver.getInstance().getScheduler().cancelTask(spigot.getShutdownSchedulerId()); //Cancelling stop ask
        }

        int percent = service.getTask().getPercentForNewServer();

        if (percent <= 100 && (((double) Bukkit.getOnlinePlayers().size()) / (double) Bukkit.getMaxPlayers()) * 100 >= percent) {

            int onlineServices = service.getTask().getOnlineServices().size();
            int maxOnlineService = service.getTask().getMaxOnlineService();

            if (onlineServices >= maxOnlineService) {
                //already enough services online do not start a new one
                return;
            }

            //Starting new service of this group with timeout of 5 minutes
            service.getTask()
                    .configureFutureService()
                    .timeOutIfNoPlayers(300)
                    .start();


        }

        RemoteAdapter adapter = Remote.getInstance().getAdapter();
        if (adapter.getLoginChecker() != null) {
            LoginCheckResult loginResult = adapter.getLoginChecker().supply(cloudPlayer);
            if (loginResult.isLoginDenied()) {
                cloudPlayer.asProxyPlayer().disconnect(loginResult.getReason());
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                //event.setKickMessage(loginResult.getReason());
                return;
            } else {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            }
        }
    }
}
