package cloud.hytora.plugins.smartproxy.bungee.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.plugins.smartproxy.bungee.BungeeBootstrap;
import lombok.var;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;

public class PlayerInjectListener implements Listener {

    @EventHandler (priority = -128)
    public void onPlayerHandshakeEvent(PlayerHandshakeEvent event) {
        this.injectConnection(event.getConnection());
    }
    @EventHandler (priority = EventPriority.LOW)
    public void onPreLoginEvent(PreLoginEvent event) {
        this.injectConnection(event.getConnection());
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onProxyPingEvent(ProxyPingEvent event) {
        this.injectConnection(event.getConnection());
    }

    /**
     * Injects a custom ip into this connection
     *
     * @param connection the connection
     */
    private void injectConnection(PendingConnection connection) {

        var address = BungeeBootstrap.getInstance().getAddresses().get(connection.getAddress());

        if (address == null) return;
        try {
            Field wrapperField = connection.getClass().getDeclaredField("ch");
            wrapperField.setAccessible(true);
            Object wrapper = wrapperField.get(connection);
            Field addressField = wrapper.getClass().getDeclaredField("remoteAddress");
            addressField.setAccessible(true);
            addressField.set(wrapper, address);
            CloudPlayer cloudPlayer;
            if (connection.getName() == null) {

                cloudPlayer = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().stream().filter(cp -> cp.getConnection().getAddress().equals(ProtocolAddress.fromSocketAddress(address))).findFirst().orElse(null);

                if (cloudPlayer == null) {
                    return;
                }
            }

            cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(connection.getName());

            PlayerConnection playerConnection = cloudPlayer.getConnection();

            ((DefaultPlayerConnection)playerConnection).setAddress(ProtocolAddress.fromSocketAddress(connection.getAddress()));
            ((UniversalCloudPlayer)cloudPlayer).setConnection(playerConnection);
            cloudPlayer.update();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

