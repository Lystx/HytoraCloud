package cloud.hytora.modules.proxy.bungee.listener;

import cloud.hytora.modules.proxy.bungee.BungeeCloudProxyPlugin;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.annotation.Nonnull;


public class BungeeCloudProxyListener implements Listener {

	@EventHandler
	public void onLogin(@Nonnull LoginEvent event) {
		BungeeCloudProxyPlugin.getInstance().getManager().updateTabList();
	}

	@EventHandler
	public void onDisconnect(@Nonnull PlayerDisconnectEvent event) {
		BungeeCloudProxyPlugin.getInstance().getManager().updateTabList();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(@Nonnull ProxyPingEvent event) {
		event.setResponse(BungeeCloudProxyPlugin.getInstance().getManager().getMotd(event.getResponse()));
	}

}
