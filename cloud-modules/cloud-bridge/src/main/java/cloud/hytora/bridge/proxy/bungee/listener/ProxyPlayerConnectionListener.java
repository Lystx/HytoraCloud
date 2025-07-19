package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.bridge.proxy.bungee.BungeeBootstrap;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.LoggingDriver;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.entity.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;

import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.LoginCheckResult;
import cloud.hytora.remote.adapter.RemoteAdapter;
import cloud.hytora.remote.impl.RemotePlayerManager;
import lombok.AllArgsConstructor;
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
        PacketCloudEntityPlayer.forProxyPlayerDisconnect(event.getPlayer().getUniqueId())
                .sendQuery()
                .execute()
                .onTaskSucess(bufferedResponse -> {
                    //quick register this is specific to proxy. other services get updated cache after a few milliseconds from node
                    ((RemotePlayerManager) CloudDriver.getInstance().getPlayerManager()).unregister(event.getPlayer().getUniqueId());
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
            UniversalCloudMessages cloudMessages = CloudDriver.getInstance().getConfigManager().getConfig().getMessages();
            event.getPlayer().disconnect(new TextComponent(ComponentColor.translateAlternateColorCodes('&', (cloudMessages.getPrefix() + cloudMessages.getNoAvailableFallbackMessage()))));
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

        PendingConnection connection = event.getConnection();

        DefaultPlayerConnection defaultPlayerConnection = new DefaultPlayerConnection(
                event.getConnection().getUniqueId(),
                event.getConnection().getName(),
                CloudDriver.getInstance().getServiceManager().thisService().getName(),
                ProtocolAddress.fromString(connection.getSocketAddress().toString()),
                connection.getVersion(),
                connection.isOnlineMode(),
                connection.isOnlineMode()
        );

        UniversalCloudMessages messages = CloudDriver.getInstance().getConfigManager().getConfig().getMessages();

        //choosing the first server this player gets connected to when joining
        CloudService firstJoinServer = CloudDriver
                .getInstance()
                .getServiceManager()
                .getFallbackAsService().orElse(null);
        if (firstJoinServer == null) { //what if somehow no fallback has been found? big mistake! we shouldn't allow
            connection.disconnect(messages.getPrefix() + messages.getNoAvailableFallbackMessage());  //players like that on the network!
            return;
        }
        bungeeBootstrap.setFirstJoinServer(defaultPlayerConnection.getConnectionId(), firstJoinServer);

        BufferedResponse loginResponse = PacketCloudEntityPlayer.forProxyLoginRequest(defaultPlayerConnection, firstJoinServer).sendQuery().execute().syncUninterruptedly().get();
        PacketBuffer buffer = loginResponse.buffer();

        CloudPlayer player = buffer.readOptionalObject(UniversalCloudPlayer.class);

        String kickReason = buffer.readOptionalString();

        if (player == null) {
            kickReason = messages.getPrefix() + messages.getNoCloudPlayerFoundLogin() + " §8[§eState: " + loginResponse.state() + "§8]";
        }

        RemoteAdapter adapter = Remote.getInstance().getAdapter();
        if (adapter.getLoginChecker() != null) {
            LoginCheckResult loginResult = adapter.getLoginChecker().supply(player);
            if (loginResult.isLoginDenied()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(loginResult.getReason()));
                event.completeIntent(bungeeBootstrap);
                PacketCloudEntityPlayer.forProxyLoginFailed(event.getConnection().getUniqueId(), Remote.getInstance().thisService().getName(), kickReason).publish();
                return;
            }
        }

        if (kickReason != null) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText(kickReason));
            event.completeIntent(bungeeBootstrap);
            PacketCloudEntityPlayer.forProxyLoginFailed(event.getConnection().getUniqueId(), Remote.getInstance().thisService().getName(), kickReason).publish();
            return;
        }

        ((RemotePlayerManager) CloudDriver.getInstance().getPlayerManager()).registerPlayer(player);
        info("Player[name={}, uuid={}] logged in on this Proxy!", player.getName(), player.getUniqueId());
        event.completeIntent(bungeeBootstrap);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PostLoginEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();

        PendingConnection connection = proxiedPlayer.getPendingConnection();
        DefaultPlayerConnection defaultPlayerConnection = new DefaultPlayerConnection(
                proxiedPlayer.getUniqueId(),
                proxiedPlayer.getName(),
                CloudDriver.getInstance().getServiceManager().thisService().getName(),
                ProtocolAddress.fromString(connection.getSocketAddress().toString()),
                connection.getVersion(),
                connection.isOnlineMode(),
                connection.isOnlineMode()
        );

        CloudService firstJoinServer = bungeeBootstrap.getFirstJoinServer(proxiedPlayer.getUniqueId());

        PacketCloudEntityPlayer.forProxyLoginSuccess(proxiedPlayer.getUniqueId(), Remote.getInstance().thisService().getName(), firstJoinServer.getName(), defaultPlayerConnection).publish();
        bungeeBootstrap.removeFirstJoin(proxiedPlayer.getUniqueId());


        if (CloudDriver.getInstance().getServiceManager().thisService().getTask().isMaintenance()) {
            proxiedPlayer.sendMessage(ComponentColor.translateAlternateColorCodes('&', (CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getPrefix() + CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getMaintenanceKickByPassedMessage())));
        }

    }

}
