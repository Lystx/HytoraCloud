package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.event.ComponentEvent;
import cloud.hytora.driver.component.event.click.ClickAction;
import cloud.hytora.driver.component.event.hover.HoverAction;
import cloud.hytora.driver.component.style.ComponentStyle;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.executor.PlayerExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Command({"players", "player"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.command.use")
@CommandAutoHelp
@CommandDescription("Manages all players")
@ApplicationParticipant
public class PlayerCommand {


    @Command("list")
    @CommandDescription("Lists all players")
    public void executeList(CommandSender sender) {

        List<ICloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("§cThere are currently no players online!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Players (" + players.size() + ")§8:");

        for (ICloudPlayer player : players) {
            sender.sendMessage("§b" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        sender.sendMessage("§8");
    }

    @Command("info")
    @Syntax("<name>")
    @CommandDescription("debug command")
    public void executeInfo(CommandSender sender, @Argument("name") String name) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();
        CloudOfflinePlayer player = playerManager.getOfflinePlayerByNameBlockingOrNull(name);

        if (player == null) {
            sender.sendMessage("§cNo such player with the name §e" + name + " §chas ever joined the network!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

        sender.sendMessage("§8");
        sender.sendMessage("Service information:");
        sender.sendMessage("§bName: §7" + player.getName() + " §8[§3" + player.getUniqueId() + "§8]");
        sender.sendMessage("§bFirst Login: §7" +  sdf.format(new Date(player.getFirstLogin())));
        sender.sendMessage("§bLast Login: §7" +  sdf.format(new Date(player.getLastLogin())));
        sender.sendMessage("§bProperties: §7" +  player.getProperties().asRawJsonString());
        sender.sendMessage("§bStatus: §7" + (player.isOnline() ? "§aOnline" : "§cOffline"));
        if (player.isOnline()) {
            ICloudPlayer onlinePlayer = player.asOnlinePlayer();
            sender.sendMessage("§bProxy: §7" + onlinePlayer.getProxyServer());
            sender.sendMessage("§bServer: §7" + onlinePlayer.getServer());

        }
        sender.sendMessage("§8");
    }
}
