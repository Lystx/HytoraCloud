package cloud.hytora.modules.proxy.velocity;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatColor;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.modules.proxy.config.MotdLayOut;
import cloud.hytora.modules.proxy.helper.AbstractCloudProxyManager;
import cloud.hytora.remote.Remote;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.server.ServerPing.Players;
import com.velocitypowered.api.proxy.server.ServerPing.SamplePlayer;
import com.velocitypowered.api.proxy.server.ServerPing.Version;
import net.kyori.text.TextComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class VelocityCloudProxyManager extends AbstractCloudProxyManager {

	private final VelocityCloudProxyPlugin plugin;

	public VelocityCloudProxyManager(VelocityCloudProxyPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void updateTabList() {
		for (Player player : plugin.getServer().getAllPlayers()) {
			player.getTabList().setHeaderAndFooter(
				TextComponent.of(replaceVelocityPlayer(header, player)),
				TextComponent.of(replaceVelocityPlayer(footer, player))
			);
		}
	}

	@Nonnull
	public String replaceVelocityPlayer(@Nonnull String content, @Nonnull Player player) {
		return replacePlayer(content, player.getUniqueId())
			.replace("{service}", !player.getCurrentServer().isPresent() ? "N/A" : player.getCurrentServer().get().getServerInfo().getName())
			.replace("{name}", player.getUsername())
			.replace("{ping}", String.valueOf(player.getPing()))
		;
	}

	@Override
	public void schedule(@Nonnull Runnable command, long millis) {
		plugin.getServer().getScheduler().buildTask(plugin, command).repeat(millis, TimeUnit.MILLISECONDS);
	}

	@Nonnull
	public ServerPing getMotd(@Nonnull ServerPing original) {
		MotdLayOut motd = getMotdEntry();

		ServiceInfo server = Remote.getInstance().thisService();
		ServerConfiguration configuration = server.getConfiguration();


		if (configuration == null || motd == null) {
			return original;
		}

		String motdText = replaceDefault(motd.getFirstLine() + '\n' + motd.getSecondLine());
		String protocolText = motd.getProtocolText() == null ? null : replaceDefault(motd.getProtocolText());

		SamplePlayer[] playerInfo = new SamplePlayer[motd.getPlayerInfo() == null || motd.getPlayerInfo().isEmpty() ? 0 : motd.getPlayerInfo().size()];
		for (int i = 0; i < playerInfo.length; i++) {
			playerInfo[i] = new SamplePlayer(motd.getPlayerInfo().get(i), UUID.randomUUID());
		}

		return new ServerPing(
			new Version(protocolText == null ? original.getVersion().getProtocol() : 1, ChatColor.translateAlternateColorCodes('&', protocolText == null ? original.getVersion().getName() : protocolText)),
			new Players(configuration.getDefaultMaxPlayers(), CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount(), Arrays.asList(playerInfo)),
			TextComponent.of(motdText),
			original.getFavicon().orElse(null)
		);
	}
}
