package cloud.hytora.modules.proxy.helper;


import cloud.hytora.common.collection.IRandom;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.driver.DriverStorageUpdateEvent;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.modules.proxy.config.MotdLayOut;
import cloud.hytora.modules.proxy.config.ProxyConfig;
import cloud.hytora.modules.proxy.config.TabListFrame;
import cloud.hytora.remote.Remote;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;


public abstract class AbstractCloudProxyManager {

	protected int tablistAnimationIndex = 0;

	protected ProxyConfig config;

	protected String header;
	protected String footer;



	protected AbstractCloudProxyManager() {
		this.config = CloudDriver.getInstance().getStorage().get("proxyConfig", ProxyConfig.class, ProxyConfig.defaultConfig());

		CloudDriver.getInstance().getEventManager().registerListener(this);
	}


	@EventListener
	public void handle(DriverStorageUpdateEvent event) {
		this.config = CloudDriver.getInstance().getStorage().get("proxyConfig", ProxyConfig.class);

		CloudDriver.getInstance().getLogger().info("Updated ProxyConfig through DriverStorageUpdateEvent");
	}


	public abstract void updateTabList();

	public abstract void schedule(@Nonnull Runnable command, long millis);

	public void init() {
		this.scheduleTabList();
	}

	protected void scheduleTabList() {
		schedule(this::updateTabList0, (long) (config.getTablist().getAnimationInterval() * 1000));
	}

	protected void updateTabList0() {
		if (config.getTablist().getFrames().size() >= tablistAnimationIndex++)
			tablistAnimationIndex = 0;

		TabListFrame frame = config.getTablist().getFrames().get(tablistAnimationIndex);

		header = frame.getHeader();
		footer = frame.getFooter();

		header = header.replaceAll("&", "§");
		footer = footer.replaceAll("&", "§");

		updateTabList();
	}

	protected String replacePlayer(@Nonnull String content, @Nonnull UUID playerUniqueId) {
		return replaceDefault(content);
	}

	protected String replaceDefault(@Nonnull String content) {
		content = content.replaceAll("&", "§");

		ServiceInfo server = Remote.getInstance().thisService();
		ServiceTask task = server.getTask();

		int maxPlayers = -1;
		if (task != null) {
			maxPlayers = task.getDefaultMaxPlayers();
		}


		return content
			.replace("{proxy}", server.getName())
			.replace("{node}", task.getNode())
			.replace("{players.online}", CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() + "")
			.replace("{players.max}", maxPlayers + "")
		;
	}

	@Nullable
	public MotdLayOut getMotdEntry() {
		ServiceInfo serviceInfo = Remote.getInstance().thisService();
		ServiceTask task = serviceInfo.getTask();

		if (task == null) {
			return null;
		}
		List<MotdLayOut> elements = task.isMaintenance() ? config.getMotd().getMaintenances() : config.getMotd().getDefaults();
		if (elements.isEmpty()) {
			return null;
		}
		return IRandom.singleton().choose(elements);
	}

}
