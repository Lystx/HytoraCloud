package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.bridge.proxy.bungee.BungeeBootstrap;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.LoggingDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import cloud.hytora.remote.Remote;
import cloud.hytora.remote.impl.RemotePlayerManager;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Comparator;
import java.util.Optional;

@AllArgsConstructor
public class ProxyPlayerConnectionListener implements Listener, LoggingDriver {


    /**
     * THe BungeeCordPlugin used for Intents
     */
    private final BungeeBootstrap bungeeBootstrap;


    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        bungeeBootstrap.removeFirstJoin(event.getPlayer().getUniqueId());
        PacketCloudPlayer.forProxyPlayerDisconnect(event.getPlayer().getUniqueId())
                .awaitResponse()
                .onTaskSucess(bufferedResponse -> {
                    //quick register this is specific to proxy. other services get updated cache after a few milliseconds from node
                    ((RemotePlayerManager)CloudDriver.getInstance().getPlayerManager()).unregister(event.getPlayer().getUniqueId());
                    debug("Disconnected Player[name={} uuid={}]", event.getPlayer().getName(), event.getPlayer().getUniqueId());
                }).onTaskFailed(e -> {
                    error("Could not disconnect Player[name={}, uuid={}] because : {}", event.getPlayer().getName(), event.getPlayer().getUniqueId(), e.getMessage());
                });
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Optional<ServerInfo> fallback = CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getTask().getVersion().isProxy())
                .filter(service -> service.getTask().getFallback().isEnabled())
                .filter(service -> (player.getServer() == null || !player.getServer().getInfo().getName().equals(service.getName())))
                .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                .map(service -> ProxyServer.getInstance().getServerInfo(service.getName()));

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


    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(LoginEvent event) {
        event.registerIntent(bungeeBootstrap);


        BufferedResponse loginResponse = PacketCloudPlayer.forProxyLoginRequest(event.getConnection().getUniqueId(), event.getConnection().getName()).awaitResponse().syncUninterruptedly().get();
        PacketBuffer buffer = loginResponse.buffer();

        ICloudPlayer player = buffer.readOptionalObject(UniversalCloudPlayer.class);
        String kickReason = buffer.readOptionalString();

        if (player == null) {
            kickReason = "§cLogin-Query returned no CloudPlayer! §8[§eState: " + loginResponse.state() + "§8]";
        }

        if (kickReason != null) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText(kickReason));
            event.completeIntent(bungeeBootstrap);
            PacketCloudPlayer.forProxyLoginFailed(event.getConnection().getUniqueId(), Remote.getInstance().thisService().getName(), kickReason).publish();
            return;
        }

        ((RemotePlayerManager) CloudDriver.getInstance().getPlayerManager()).registerPlayer(player);
        info("Player[name={}, uuid={}] logged in on this Proxy!", player.getName(), player.getUniqueId());

        event.completeIntent(bungeeBootstrap);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PostLoginEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();

        ICloudService firstJoinServer = bungeeBootstrap.getFirstJoinServer(proxiedPlayer.getUniqueId());


        PacketCloudPlayer.forProxyLoginSuccess(proxiedPlayer.getUniqueId(), Remote.getInstance().thisService().getName(), firstJoinServer.getName()).publish();
        bungeeBootstrap.removeFirstJoin(proxiedPlayer.getUniqueId());
    }

}
