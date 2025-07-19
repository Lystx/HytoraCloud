package cloud.hytora.bridge.proxy.bungee.command;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import javax.annotation.Nonnull;
import java.util.Collection;

public class BungeeCommand extends Command implements TabExecutor {

	public BungeeCommand(@Nonnull String name, @Nonnull Collection<DriverCommandInfo> commands) {
		super(name);
	}

	@Override
	public void execute(@Nonnull CommandSender sender, @Nonnull String[] args) {
		if (!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		PacketCloudEntityPlayer.forPlayerCommandExecute(player.getUniqueId(), getName() + (args.length == 0 ? "" : " ") + StringUtils.getArrayAsString(args, " ")).publishAsync();
	}

	@Override
	public Iterable<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull String[] args) {
		if (!(sender instanceof ProxiedPlayer)) return null;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		BufferedResponse bufferedResponse = PacketCloudEntityPlayer.forPlayerTabComplete(player.getUniqueId(), getName() + (args.length == 0 ? "" : " ") + StringUtils.getArrayAsString(args, " ")).sendQuery().execute().syncUninterruptedly().get();
		return bufferedResponse.buffer().readStringCollection();
	}
}
