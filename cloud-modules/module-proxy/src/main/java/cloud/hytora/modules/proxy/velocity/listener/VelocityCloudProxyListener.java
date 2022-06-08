package cloud.hytora.modules.proxy.velocity.listener;

import cloud.hytora.modules.proxy.velocity.VelocityCloudProxyPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;

import javax.annotation.Nonnull;


public class VelocityCloudProxyListener {

	@Subscribe
	public void onLogin(@Nonnull LoginEvent event) {
		VelocityCloudProxyPlugin.getInstance().getManager().updateTabList();
	}

	@Subscribe
	public void onDisconnect(@Nonnull DisconnectEvent event) {
		VelocityCloudProxyPlugin.getInstance().getManager().updateTabList();
	}

	@Subscribe
	public void onPing(@Nonnull ProxyPingEvent event) {
		event.setPing(VelocityCloudProxyPlugin.getInstance().getManager().getMotd(event.getPing()));
	}

}
