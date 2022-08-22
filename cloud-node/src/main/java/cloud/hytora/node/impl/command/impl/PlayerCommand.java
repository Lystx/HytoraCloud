package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.ArgumentHelp;
import cloud.hytora.driver.commands.help.ArgumentHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Command(
        label = "player",
        aliases = {"p", "players"},
        desc = "Manages all players",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)
public class PlayerCommand {

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("player");
    }

    @Command(
            label = "list",
            parent = "player",
            desc = "Lists all online players",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void listCommand(CommandContext<?> ctx, CommandArguments args) {

        List<ICloudPlayer> players = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            ctx.sendMessage("§cThere are currently no players online!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("§7Players (" + players.size() + ")§8:");

        for (ICloudPlayer player : players) {
            ctx.sendMessage("§b" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        ctx.sendMessage("§8");
    }


    @Command(
            label = "list",
            parent = "player",
            usage = "<name>",
            desc = "Gives info about a player",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {
        String name = args.get(0, String.class);
        ICloudPlayerManager playerManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class);
        CloudOfflinePlayer player = playerManager.getOfflinePlayerByNameBlockingOrNull(name);

        if (player == null) {
            ctx.sendMessage("§cNo such player with the name §e" + name + " §chas ever joined the network!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

        ctx.sendMessage("§8");
        ctx.sendMessage("Service information:");
        ctx.sendMessage("§bName: §7" + player.getName() + " §8[§3" + player.getUniqueId() + "§8]");
        ctx.sendMessage("§bFirst Login: §7" +  sdf.format(new Date(player.getFirstLogin())));
        ctx.sendMessage("§bLast Login: §7" +  sdf.format(new Date(player.getLastLogin())));
        ctx.sendMessage("§bProperties: §7" +  player.getProperties().asRawJsonString());
        ctx.sendMessage("§bStatus: §7" + (player.isOnline() ? "§aOnline" : "§cOffline"));
        if (player.isOnline()) {
            ICloudPlayer onlinePlayer = player.asOnlinePlayer();
            ctx.sendMessage("§bProxy: §7" + onlinePlayer.getProxyServer());
            ctx.sendMessage("§bServer: §7" + onlinePlayer.getServer());

        }
        ctx.sendMessage("§8");
    }
}
