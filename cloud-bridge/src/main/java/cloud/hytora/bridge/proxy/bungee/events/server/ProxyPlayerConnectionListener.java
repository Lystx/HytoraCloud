package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.bridge.proxy.bungee.BungeeBootstrap;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    public void handle(ServerKickEvent event) {
        Optional<ServerInfo> fallback = this.getFallback(event.getPlayer());

        if (!fallback.isPresent()) {
            CloudMessages cloudMessages = CloudMessages.getInstance();
            event.getPlayer().disconnect(new TextComponent(cloudMessages.getPrefix() + " " + cloudMessages.getNoAvailableFallbackMessage()));
            return;
        }
        fallback.ifPresent(serverInfo -> {
            event.setCancelled(true);
            event.setCancelServer(serverInfo);
        });
    }


    private Optional<ServerInfo> getFallback(ProxiedPlayer player) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getTask().getVersion().isProxy())
                .filter(service -> service.getTask().getFallback().isEnabled())
                .filter(service -> (player.getServer() == null || !player.getServer().getInfo().getName().equals(service.getName())))
                .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                .map(service -> ProxyServer.getInstance().getServerInfo(service.getName()));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PreLoginEvent event) {
        event.registerIntent(bungeeBootstrap);

        AtomicReference<ICloudPlayer> cloudPlayer = new AtomicReference<>(null);

        CloudDriver.getInstance()
                .getPlayerManager()
                .getOfflinePlayer(event.getConnection().getName())
                .onTaskSucess(op -> {
                    if (op == null) {
                        System.out.println("Tried to load a Player that has never joined before! ");
                        System.out.println("This warning is harmless and can be ignored!");
                        return;
                    }
                    String name = event.getConnection().getName();
                    CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}] because he has joined before!", op.getUniqueId(), op.getName());

                    CloudDriver.getInstance().getPlayerManager().registerCloudPlayer(op.getUniqueId(), name);
                    cloudPlayer.set(CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(op.getUniqueId()));
                }).onTaskFailed(e -> {
                });

        ICloudService cloudServer = CloudDriver.getInstance().getServiceManager().thisServiceOrNull();
        IServiceTask serviceTask = cloudServer.getTask();

        List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);

        if (event.getConnection().getName() != null) {
            if (serviceTask.isMaintenance() && !whitelistedPlayers.contains(event.getConnection().getName()) && !(cloudPlayer.get() != null && cloudPlayer.get().hasPermission("cloud.maintenance.bypass"))) {
                event.setCancelReason(new TextComponent("§cThe network is currently in maintenance!\nCome back later!"));
                event.setCancelled(true);
                event.completeIntent(bungeeBootstrap);
                return;
            }
        }

        if (cloudServer.getOnlinePlayerCount() >= cloudServer.getMaxPlayers()) {
            if (cloudPlayer.get() != null) {
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(PlayerFullJoinExecutor.class).execute(cloudPlayer.get(), false, true);

            }
            event.setCancelReason(new TextComponent("§cThis Proxy is currently full!"));
            event.setCancelled(true);
            event.completeIntent(bungeeBootstrap);
            return;
        }

        Task<ICloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

        if (fallback.isNull()) {
            event.setCancelReason(new TextComponent("§cCould not find any fallback to connect you to..."));
            event.setCancelled(true);
            event.completeIntent(bungeeBootstrap);
            return;
        }

        event.completeIntent(bungeeBootstrap);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(LoginEvent event) {
        PendingConnection c = event.getConnection();
        if (CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(c.getName()) != null) {
            return;
        }
        CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}]", c.getUniqueId(), c.getName());
        playerManager.registerCloudPlayer(c.getUniqueId(), c.getName());

    }

}
