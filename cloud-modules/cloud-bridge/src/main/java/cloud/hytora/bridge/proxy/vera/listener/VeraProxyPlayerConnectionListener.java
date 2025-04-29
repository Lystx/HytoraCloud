package cloud.hytora.bridge.proxy.vera.listener;

import cloud.hytora.bridge.proxy.vera.VeraBootstrap;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import de.verasoftware.proxy.api.VeraProxy;
import de.verasoftware.proxy.api.component.ChatComponent;
import de.verasoftware.proxy.api.environment.entity.player.Player;
import de.verasoftware.proxy.api.event.annotation.Listener;
import de.verasoftware.proxy.api.event.defaults.player.PlayerLoginEvent;
import de.verasoftware.proxy.api.event.defaults.player.PlayerLogoutEvent;
import de.verasoftware.proxy.api.event.defaults.player.PreLoginEvent;
import de.verasoftware.proxy.api.event.defaults.server.PlayerServerChooseEvent;
import de.verasoftware.proxy.api.event.defaults.service.ServiceDisconnectEvent;
import de.verasoftware.proxy.api.server.ProxyServer;

import java.util.Comparator;
import java.util.Optional;

public class VeraProxyPlayerConnectionListener {


    private final PlayerManager playerManager;
    private final VeraBootstrap veraBootstrap;

    public VeraProxyPlayerConnectionListener(VeraBootstrap bungeeBootstrap) {
        this.veraBootstrap = bungeeBootstrap;
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }


    @Listener
    public void handle(PlayerLogoutEvent event) {
        veraBootstrap.removeFirstJoin(event.getPlayer().getUniqueId());
    }

   @Listener
    public void handle(ServiceDisconnectEvent event) {
        Optional<ProxyServer> fallback = this.getFallback(event.getConnection().getPlayer());

        if (!fallback.isPresent()) {
            CloudMessages cloudMessages = CloudMessages.getInstance();
            event.getConnection().getPlayer().disconnect(ChatComponent.text(cloudMessages.getPrefix() + " " + cloudMessages.getNoAvailableFallbackMessage()));
            return;
        }
        fallback.ifPresent(serverInfo -> {
            event.setCancelled(true);
            event.getConnection().getPlayer().connect(serverInfo);
        });
    }



    private Optional<ProxyServer> getFallback(Player player) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getTask().getVersion().isProxy())
                .filter(service -> service.getTask().getFallback().isEnabled())
                .filter(service -> (player.getConnectedServer() == null || !player.getConnectedServer().getName().equals(service.getName())))
                .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                .map(service -> VeraProxy.getInstance().getRegisteredServer(service.getName()));
    }


    @Listener
    public void handle(PlayerServerChooseEvent event) {
        Player player = event.getPlayer();

        ICloudService fallback = CloudDriver
                .getInstance()
                .getServiceManager()
                .getFallbackAsService().orElse(null);
        if (fallback == null) { //what if somehow no fallback has been found? big mistake! we shouldn't allow
            player.disconnect(ChatComponent.text("§cError 3825"));  //players like that on the network!
            return;
        }
        veraBootstrap.setFirstJoinServer(player.getUniqueId(), fallback);
        event.setServer(VeraProxy.getInstance().getRegisteredServer(fallback.getName()));
    }


    @Listener()
    public void handle(PreLoginEvent event) {

        ICloudPlayer cloudPlayer = null;

        /*if (Remote.getInstance().getProperty().getPlayerLoginProcessing() == PlayerLoginProcessing.UUID_CACHE && CloudDriver.getInstance().getUUIDCache().getCachedId(event.getPlayerName()) != null) {
            String name = event.getPlayerName();
            UUID uuid = event.getPlayerUniqueId();

            CloudDriver.getInstance().getPlayerManager().registerCloudPlayer(uuid, name);
            cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(uuid);
        }

        ICloudService cloudServer = CloudDriver.getInstance().getServiceManager().thisServiceOrNull();
        IServiceTask serviceTask = cloudServer.getTask();

        List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);

        if (event.getPlayerName() != null) {
            if (serviceTask.isMaintenance() && !whitelistedPlayers.contains(event.getPlayerName()) && !(cloudPlayer != null && cloudPlayer.hasPermission("cloud.maintenance.bypass"))) {
                event.setCancelReason(ChatComponent.text("§cThe network is currently in maintenance!\nCome back later!"));
                event.setCancelled(true);
                return;
            }
        }

        if (cloudServer.getOnlinePlayerCount() >= cloudServer.getMaxPlayers()) {
            if (cloudPlayer != null) {
                CloudDriver.getInstance().getUnchecked(PlayerFullJoinExecutor.class).execute(cloudPlayer, false, true);

            }
            event.setCancelReason(ChatComponent.text("§cThis Proxy is currently full!"));
            event.setCancelled(true);
            return;
        }

        Task<ICloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

        if (fallback.isNull()) {
            event.setCancelReason(ChatComponent.text("§cCould not find any fallback to connect you to..."));
            event.setCancelled(true);
            return;
        }*/

    }

    @Listener
    public void handle(PlayerLoginEvent event) {
        Player player = event.getPlayer();
       /*PlayerIdCache cache = CloudDriver.getInstance().getUUIDCache();


        if (cache.getCachedId(player.getName()) == null || !cache.getCachedId(player.getName()).equals(player.getUniqueId())) {
            cache.setCachedId(player.getName(), player.getUniqueId());
            cache.updateCache();
        }

        if (CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getName()) != null) {
            return;
        }
        CloudDriver.getInstance().getLogger().info("Logging in Player[uuid={}, name={}]", player.getUniqueId(), player.getName());
        playerManager.registerCloudPlayer(player.getUniqueId(), player.getName());*/
    }

}
