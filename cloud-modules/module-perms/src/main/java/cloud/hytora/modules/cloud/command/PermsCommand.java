package cloud.hytora.modules.cloud.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.permission.PermissionPlayer;

@Command(
        name = {"perms", "perm", "permissions"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.module.perms.command.use"
)
@CommandDescription("Manages the perms-module")
@CommandAutoHelp
public class PermsCommand {


    @SubCommand("player info <player>")
    public void onPlayerInfo(CommandSender sender, @CommandArgument("player") PermissionPlayer player) {

        if (player == null) {
            sender.sendMessage("§cThere is no such player registered in the modules' database!");
        }
    }
}
