package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatColor;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.ServicePingProperties;
import cloud.hytora.remote.Remote;
import net.md_5.bungee.api.Favicon;
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
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;
import java.util.stream.Collectors;

public class ProxyEvents implements Listener {

    private final PlayerManager playerManager;

    public ProxyEvents() {
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }

    @EventHandler
    public void handle(PreLoginEvent event) {
        System.out.println("PRE");
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

        Task<ServiceInfo> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

        if (fallback.isNull()) {
            event.setCancelReason(new TextComponent("§cCould not find any fallback to connect you to..."));
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void handle(PostLoginEvent event) {
        System.out.println("POST");
        ProxiedPlayer player = event.getPlayer();

    }

    @EventHandler
    public void handle(LoginEvent event) {
        System.out.println("LOGIN");
        PendingConnection c = event.getConnection();
        CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}]", c.getUniqueId(), c.getName());
        playerManager.registerCloudPlayer(c.getUniqueId(), c.getName());

    }

    public void sendToFallback(ProxiedPlayer player) {
        Task<ServiceInfo> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();
        if (fallback.isPresent()) {
            System.out.println(ProxyServer.getInstance().getServers().values().stream().map(ServerInfo::getName).collect(Collectors.toList()));
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(fallback.get().getName());
            if (serverInfo == null) {
                System.out.println("Server info is null");
            } else {
                player.connect(serverInfo);
                System.out.println("Changed fallback to => " + fallback.get().getName() + " [" + serverInfo.getAddress().toString() + "]"); // TODO: 06.06.2022 fix double process starting of proxy
            }
        } else {
            System.out.println("Couldn't find any fallback");
            player.sendMessage(new TextComponent("§cCould not find any available fallback..."));
        }
    }

    @EventHandler
    public void handle(ServerConnectEvent event) {
        System.out.println("CONNECT");
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        ServiceInfo server = serviceManager.getServiceByNameOrNull(target.getName());

        if (server != null && !(server.getTask().getPermission() == null || player.hasPermission(server.getTask().getPermission()))) {
            //kick player
            event.setCancelled(true);
            player.sendMessage("§cThis server has a specific permission to join it"); // TODO: 15.05.2022 customizable 
        }
        event.setCancelled(false);

    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        System.out.println("CONNECTED");
        Server server = event.getServer();
        ProxiedPlayer player = event.getPlayer();

        CloudPlayer cloudPlayer = this.playerManager.getCloudPlayerByUniqueIdOrNull(player.getUniqueId());
        if (cloudPlayer == null) {
            return;
        }

        //setting new service
        ServiceInfo service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(server.getInfo().getName());
        if (service != null) {
            cloudPlayer.setServer(service);
            cloudPlayer.setProxyServer(Remote.getInstance().thisService());
            cloudPlayer.update();
        }
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        System.out.println("DISCONNECT");
        playerManager.unregisterCloudPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(ProxyPingEvent event) {
        ServerPing response = event.getResponse();

        ServiceInfo serviceInfo = Remote.getInstance().thisService();
        ServicePingProperties pingProperties = serviceInfo.getPingProperties();

        int maxPlayers, onlinePlayers;
        if (pingProperties.isUsePlayerPropertiesOfService()) {
            maxPlayers = serviceInfo.getMaxPlayers();
            onlinePlayers = pingProperties.isCombineAllProxiesIfProxyService() ? CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() : serviceInfo.getOnlinePlayers();
        } else {
            maxPlayers = pingProperties.getCustomMaxPlayers();
            onlinePlayers = pingProperties.getCustomOnlinePlayers();
        }

        //player info
        String[] playerInfo = pingProperties.getPlayerInfo();
        ServerPing.PlayerInfo[] info = new ServerPing.PlayerInfo[playerInfo.length];
        for (int i = 0; i < playerInfo.length; i++) {
            info[i] = new ServerPing.PlayerInfo(ChatColor.translateAlternateColorCodes('&', playerInfo[i]), UUID.randomUUID());
        }

        //player values
        ServerPing.Players pp = response.getPlayers();

        pp.setSample(info);
        pp.setOnline(onlinePlayers);
        pp.setMax(maxPlayers);


        //server icon
        String serverIconUrl = pingProperties.getServerIconUrl();
        if (serverIconUrl != null) {
            response.setFavicon(Favicon.create(serverIconUrl));
        }

        //motd
        response.setDescriptionComponent(new TextComponent(ChatColor.translateAlternateColorCodes('&', pingProperties.getMotd())));

        //protocol text
        String versionText = pingProperties.getVersionText();
        if (versionText != null && !versionText.trim().isEmpty()) {
            response.setVersion(new ServerPing.Protocol(ChatColor.translateAlternateColorCodes('&', versionText), -1));
        }

        event.setResponse(response);
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        System.out.println("KICK");
        ProxiedPlayer player = event.getPlayer();
        ServiceInfo fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsServiceOrNull();

        if (fallback == null) {
            player.disconnect(new TextComponent("§cCould not find any available fallback..."));
        } else {
            event.setCancelServer(ProxyServer.getInstance().getServerInfo(fallback.getName()));
        }
    }

}
