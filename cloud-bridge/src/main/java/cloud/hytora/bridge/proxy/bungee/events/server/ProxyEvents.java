package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.impl.RemoteServiceManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProxyEvents implements Listener {

    private final PlayerManager playerManager;

    public ProxyEvents() {
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }

    @EventHandler
    public void handle(PreLoginEvent event) {

        System.out.println(CloudDriver.getInstance().getStorage().getRawData());

        List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().get("cloud::whitelist", List.class);

        if (!whitelistedPlayers.contains(event.getConnection().getName())) {
            event.setCancelReason(new TextComponent("§cDu besitzt momentan keinen Zuganng, um das §nNetzwerk §czu betreten."));
            event.setCancelled(true);
            return;
        }

        if (!CloudDriver.getInstance().getServiceManager().getFallbackOrNullAsService().isPresent()) {
            event.setCancelReason(new TextComponent("§cEs konnte kein passender Fallback gefunden werden."));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(LoginEvent event) {
        playerManager.registerCloudPlayer(event.getConnection().getUniqueId(), event.getConnection().getName());
    }

    
    
    @EventHandler
    public void handle(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();
        
        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        CloudServer server = serviceManager.getServiceByNameOrNull(target.getName());
        
        if (server != null && !player.hasPermission(server.getConfiguration().getPermission())) {
            //kick player
            event.setCancelled(true);
            player.sendMessage("§cThis server has a specific permission to join it"); // TODO: 15.05.2022 customizable 
        }

        this.playerManager.getCloudPlayer(event.getPlayer().getUniqueId()).ifPresent(cloudPlayer -> {
            if (event.getTarget().getName().equalsIgnoreCase("fallback")) {
                Wrapper<CloudServer> optional = serviceManager.getFallbackOrNullAsService();

                if (optional.isPresent()) {
                    event.setTarget(ProxyServer.getInstance().getServerInfo(optional.get().getName()));
                    cloudPlayer.setServer(optional.get());
                    cloudPlayer.setProxyServer(Remote.getInstance().thisService());
                    cloudPlayer.update();
                } else {
                    event.getPlayer().disconnect(new TextComponent("§cEs konnte kein passender Fallback gefunden werden."));
                }

            } else {
                cloudPlayer.setServer(Objects.requireNonNull(CloudDriver.getInstance().getServiceManager()
                        .getServiceByNameOrNull(event.getTarget().getName())));
                cloudPlayer.setProxyServer(Remote.getInstance().thisService());
                cloudPlayer.update();
            }
        });
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        playerManager.unregisterCloudPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void handle(ProxyPingEvent event) {
        ServerPing response = event.getResponse();
        ServerPing.Players players = response.getPlayers();

        response.setPlayers(new ServerPing.Players(((RemoteServiceManager) CloudDriver.getInstance().getServiceManager()).thisService().getMaxPlayers(), playerManager.getCloudPlayerOnlineAmount(), players.getSample()));
        event.setResponse(response);
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        this.getFallback(event.getPlayer()).ifPresent(serverInfo -> {
            event.setCancelled(true);
            event.setCancelServer(serverInfo);
        });
    }

    private Optional<ServerInfo> getFallback(final ProxiedPlayer player) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getConfiguration().getVersion().isProxy())
                .filter(service -> service.getConfiguration().getFallback().isEnabled())
                .filter(service -> (player.getServer() == null || !player.getServer().getInfo().getName().equals(service.getName())))
                .min(Comparator.comparing(CloudServer::getOnlinePlayers))
                .map(service -> ProxyServer.getInstance().getServerInfo(service.getName()));
    }
}
