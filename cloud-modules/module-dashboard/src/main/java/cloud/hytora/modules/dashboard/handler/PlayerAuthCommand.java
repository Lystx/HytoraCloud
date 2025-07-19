package cloud.hytora.modules.dashboard.handler;

import cloud.hytora.common.random.IRandom;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;

import java.util.Base64;

@Command(
        value = {"auth", "webapi", "webtoken", "authtoken"},
        permission = "cloud.dashboard.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all players"

)
@Command.AutoHelp
public class PlayerAuthCommand {

    @Command("show")
    @Command.Syntax("<player>")
    public void onShowCommand(CommandSender sender, @Command.Argument("player") String playerName) {
        CloudOfflinePlayer player = CloudDriver.getInstance().getPlayerManager().getCachedOfflinePlayerOrRefresh(playerName);
        if (player == null) {
            sender.sendMessage("§cThis player is unknown to §eHytoraCloud§c!");
            return;
        }

        String playerToken = player.getProperties().getString("rest-api-token");
        if (playerToken == null) {
            sender.sendMessage("§cThis player doesn't have a registered §eAPI-Token §cto his name!");
        } else {
            sender.sendMessage("§8'§e{}§8' %1API-Token§8: %2{}", player.getName(), playerToken);
        }
    }

    @Command("generate")
    @Command.Syntax("<player>")
    public void onGenerateCommand(CommandSender sender, @Command.Argument("player") String playerName) {
        CloudOfflinePlayer player = CloudDriver.getInstance().getPlayerManager().getCachedOfflinePlayerOrRefresh(playerName);
        if (player == null) {
            sender.sendMessage("§cThis player is unknown to §eHytoraCloud§c!");
            return;
        }

        byte[] bytes = new byte[36];
        IRandom.singleton().nextBytes(bytes);
        String playerToken = Base64.getUrlEncoder().encodeToString(bytes);

        player.setProperty("rest-api-token", playerToken);
        player.save();

        sender.sendMessage("§7Generated %1API-Token§8: %2{}", playerToken);
    }

}
