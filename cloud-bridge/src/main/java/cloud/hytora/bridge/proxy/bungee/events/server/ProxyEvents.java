package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.impl.RemoteServiceManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyEvents implements Listener {

    private final PlayerManager playerManager;

    public ProxyEvents() {
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }

    @EventHandler
    public void handle(PreLoginEvent event) {
        /*

        List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().get("cloud::whitelist", List.class);

        if (whitelistedPlayers == null) {
            whitelistedPlayers = new ArrayList<>();
        }

        if (event.getConnection().getName() != null) {
            if (!whitelistedPlayers.contains(event.getConnection().getName())) {
                event.setCancelReason(new TextComponent("§cDu besitzt momentan keinen Zuganng, um das §nNetzwerk §czu betreten."));
                event.setCancelled(true);
                return;
            }
        }*/
        // TODO: 29.05.2022 whitelist
        CloudDriver.getInstance().getLogger().info("Available Services : {}", CloudDriver.getInstance().getServiceManager().getAllCachedServices().size());


        Task<CloudServer> fallback = CloudDriver.getInstance().getServiceManager().getFallbackOrNullAsService();

        if (fallback.isNull()) {
            event.setCancelReason(new TextComponent("§cCould not find any fallback to connect you to..."));
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void handle(LoginEvent event) {
        PendingConnection c = event.getConnection();
        CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}]", c.getUniqueId(), c.getName());
        playerManager.registerCloudPlayer(c.getUniqueId(), c.getName());
    }
    


    @EventHandler
    public void handle(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        CloudServer server = serviceManager.getServiceByNameOrNull(target.getName());

        if (server != null && !(server.getConfiguration().getPermission() == null || player.hasPermission(server.getConfiguration().getPermission()))) {
            //kick player
            event.setCancelled(true);
            player.sendMessage("§cThis server has a specific permission to join it"); // TODO: 15.05.2022 customizable 
        }

        if (event.getTarget().getName().equalsIgnoreCase("fallback")) {
            Task<CloudServer> fallback = serviceManager.getFallbackOrNullAsService();

            if (fallback.isPresent()) {
                event.setTarget(ProxyServer.getInstance().getServerInfo(fallback.get().getName()));
                System.out.println("Changed fallback to => " + fallback.get().getName());
            } else {
                System.out.println("Couldn't find any fallback");
                event.getPlayer().disconnect(new TextComponent("§cCould not find any available fallback..."));
            }
        } else {
            System.out.println("Connecting " + player.getName() + " to => " + target.getName() + " [" + target.getAddress() + "]");

        }
    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        Server server = event.getServer();
        ProxiedPlayer player = event.getPlayer();

        CloudPlayer cloudPlayer = this.playerManager.getCloudPlayerByUniqueIdOrNull(player.getUniqueId());
        if (cloudPlayer == null) {
            return;
        }

        //setting new service
        CloudServer service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(server.getInfo().getName());
        if (service != null) {
            cloudPlayer.setServer(service);
            cloudPlayer.setProxyServer(Remote.getInstance().thisService());
            cloudPlayer.update();
        }
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
        CloudDriver.getInstance().getServiceManager().getFallbackOrNullAsService().ifPresent(serverInfo -> {
            event.setCancelled(true);
            event.setCancelServer(ProxyServer.getInstance().getServerInfo(serverInfo.getName()));
        });
    }

}
