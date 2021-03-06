package de.lystx.hytoracloud.bridge.proxy.velocity.listener.player;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import de.lystx.hytoracloud.bridge.CloudBridge;

import java.lang.reflect.Field;

public class PlayerInjectListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onProxyPingEvent(ProxyPingEvent event) {
        injectConnection(event.getConnection());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerHandshakeEvent(ConnectionHandshakeEvent event) {
        injectConnection(event.getConnection());
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPreLoginEvent(PreLoginEvent event) {
        injectConnection(event.getConnection());
    }

    /**
     * Injects a custom ip into this connection
     *
     * @param connection the connection
     */
    private void injectConnection(InboundConnection connection) {
        if (CloudBridge.getInstance().getAddresses().get(connection.getRemoteAddress()) == null)
            return;
        try {
            Field wrapperField = connection.getClass().getDeclaredField("ch");
            wrapperField.setAccessible(true);
            Object wrapper = wrapperField.get(connection);
            Field addressField = wrapper.getClass().getDeclaredField("remoteAddress");
            addressField.setAccessible(true);
            addressField.set(wrapper, CloudBridge.getInstance().getAddresses().get(connection.getRemoteAddress()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
