package cloud.hytora.modules.proxy.bungee;

import cloud.hytora.modules.proxy.bungee.listener.BungeeCloudProxyListener;
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nonnull;


public class BungeeCloudProxyPlugin extends Plugin {

	private static BungeeCloudProxyPlugin instance;

	private BungeeCloudProxyManager manager;

	@Override
	public void onEnable() {
		instance = this;
		manager = new BungeeCloudProxyManager(this);
		manager.init();

		getProxy().getPluginManager().registerListener(this, new BungeeCloudProxyListener());
	}

	@Nonnull
	public BungeeCloudProxyManager getManager() {
		return manager;
	}

	public static BungeeCloudProxyPlugin getInstance() {
		return instance;
	}
}
