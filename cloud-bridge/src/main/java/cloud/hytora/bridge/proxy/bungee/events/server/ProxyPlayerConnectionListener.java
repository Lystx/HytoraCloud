package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.bridge.proxy.bungee.BungeeBootstrap;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerFullJoinChecker;
import cloud.hytora.driver.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.uuid.DriverUUIDCache;
import cloud.hytora.driver.uuid.PlayerLoginProcessing;
import cloud.hytora.remote.Remote;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.UUID;

public class ProxyPlayerConnectionListener implements Listener {


    private final PlayerManager playerManager;
    private final BungeeBootstrap bungeeBootstrap;

    public ProxyPlayerConnectionListener(BungeeBootstrap bungeeBootstrap) {
        this.bungeeBootstrap = bungeeBootstrap;
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }



    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        playerManager.unregisterCloudPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void handle(PreLoginEvent event) {
        event.registerIntent(bungeeBootstrap);

        ICloudPlayer cloudPlayer = null;

        if (Remote.getInstance().getProperty().getPlayerLoginProcessing() == PlayerLoginProcessing.UUID_CACHE && CloudDriver.getInstance().getUUIDCache().getUUID(event.getConnection().getName()) != null) {
            String name = event.getConnection().getName();
            UUID uuid = CloudDriver.getInstance().getUUIDCache().getUUID(name);

            CloudDriver.getInstance().getPlayerManager().registerCloudPlayer(uuid, name);
            cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(uuid);
        }

        ICloudServer cloudServer = CloudDriver.getInstance().getServiceManager().thisServiceOrNull();
        IServiceTask serviceTask = cloudServer.getTask();

        List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);

        if (event.getConnection().getName() != null) {
            if (serviceTask.isMaintenance() && !whitelistedPlayers.contains(event.getConnection().getName()) && !(cloudPlayer != null && cloudPlayer.hasPermission("cloud.maintenance.bypass"))) {
                event.setCancelReason(new TextComponent("§cThe network is currently in maintenance!\nCome back later!"));
                event.setCancelled(true);
                event.completeIntent(bungeeBootstrap);
                return;
            }
        }

        if (cloudServer.getOnlinePlayerCount() >= cloudServer.getMaxPlayers()) {
            if (cloudPlayer != null) {
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(PlayerFullJoinExecutor.class).execute(cloudPlayer, false, true);

            }
            event.setCancelReason(new TextComponent("§cThis Proxy is currently full!"));
            event.setCancelled(true);
            event.completeIntent(bungeeBootstrap);
            return;
        }

        Task<ICloudServer> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

        if (fallback.isNull()) {
            event.setCancelReason(new TextComponent("§cCould not find any fallback to connect you to..."));
            event.setCancelled(true);
            event.completeIntent(bungeeBootstrap);
            return;
        }

        event.completeIntent(bungeeBootstrap);
    }

    @EventHandler
    public void handle(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        DriverUUIDCache cache = CloudDriver.getInstance().getUUIDCache();


        if (cache.getUUID(player.getName()) == null || !cache.getUUID(player.getName()).equals(player.getUniqueId())) {
            cache.setUUID(player.getName(), player.getUniqueId());
            cache.update();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(LoginEvent event) {
        PendingConnection c = event.getConnection();
        if (CloudDriver.getInstance().getPlayerManager().getCloudPlayer(c.getName()).isPresent()) {
            return;
        }
        CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}]", c.getUniqueId(), c.getName());
        playerManager.registerCloudPlayer(c.getUniqueId(), c.getName());

    }

}
