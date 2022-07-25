package cloud.hytora.modules.notify.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandAutoHelp;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.SubCommand;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

@CommandAutoHelp
@Command(
        name = "notify",
        scope = CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE,
        permission = "cloud.module.notify.command.use"
)
@CommandDescription("Manages the notifications of the notify-module")
public class NotifyCommand {

    @SubCommand("toggle")
    @CommandDescription("Toggles notifications for players!")
    public void execute(PlayerCommandSender sender) {
        CloudPlayer player = sender.getPlayer();

        NotifyConfiguration configuration = NotifyModule.getInstance().getConfiguration();
        if (configuration.getEnabledNotifications().contains(player.getUniqueId())) {
            //has enabled notify ==> disabling

            configuration.getEnabledNotifications().remove(player.getUniqueId());
            player.sendMessage("§7You will §cno longer §7receive notifications§8!");
        } else {
            //has disabled notify ==> enabling

            configuration.getEnabledNotifications().remove(player.getUniqueId());
            player.sendMessage("§7You will §anow §7receive notifications§8!");
        }

        NotifyModule.getInstance().getController().getConfig().set(configuration);
        NotifyModule.getInstance().getController().getConfig().save();
    }

}
