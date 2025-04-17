package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

@Command(
        value = {"players", "player"},
        permission = "cloud.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all players"

)
@Command.AutoHelp
@ApplicationParticipant
public class PlayerCommand {


    @Command(value = "list", description = "Lists all players")
    public void executeList(CommandSender sender) {

        Collection<ICloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

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

    @Command(value = "info", description = "Shows information about a player")
    @Command.Syntax("<name>")
    public void executeInfo(CommandSender sender, @Command.Argument("name") String name) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();
        playerManager.getOfflinePlayer(name)
                .onTaskSucess(player -> {
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
                    if (!player.getProperties().has("debugged")) {
                        player.editProperties(properties -> {
                            properties.set("debugged", true);
                        });
                    }
                    sender.sendMessage("§8");
                }).onTaskFailed(e -> {});

    }
}
