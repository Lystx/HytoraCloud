package cloud.hytora.node.impl.command.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.database.impl.section.DatabaseSection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Command(
        name = {"player", "players", "ps", "p"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandAutoHelp
@CommandDescription("Manages all players")
public class PlayerCommand {


    @SubCommand("list")
    @CommandDescription("Lists all players")
    public void executeList(CommandSender sender) {

        List<CloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("§cThere are currently no players online!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Players (" + players.size() + ")§8:");

        for (CloudPlayer player : players) {
            sender.sendMessage("§b" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        sender.sendMessage("§8");
    }

    @SubCommand("info <name>")
    @CommandDescription("debug command")
    public void executeInfo(CommandSender sender, @CommandArgument("name") String name) {

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
            CloudPlayer onlinePlayer = player.asOnlinePlayer();
            sender.sendMessage("§bProxy: §7" + onlinePlayer.getProxyServer());
            sender.sendMessage("§bServer: §7" + onlinePlayer.getServer());
            sender.sendMessage("§bOnline Properties: §7" + onlinePlayer.getTemporaryProperties().asRawJsonString());
        }
        sender.sendMessage("§8");
    }
}
