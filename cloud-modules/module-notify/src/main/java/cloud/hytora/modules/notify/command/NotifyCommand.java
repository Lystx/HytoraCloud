package cloud.hytora.modules.notify.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

@Command("notify")
@CommandDescription("Manages the notifications of the notify-module")
@CommandExecutionScope(CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE)
@CommandPermission("cloud.modules.notify.command.use")
@CommandAutoHelp
public class NotifyCommand {

    @Command("toggle")
    @CommandDescription("Toggles notifications for players!")
    public void execute(PlayerCommandSender sender) {
        ICloudPlayer player = sender.getPlayer();

        NotifyConfiguration configuration = NotifyModule.getInstance().getConfiguration();
        if (configuration.getEnabledNotifications().contains(player.getUniqueId())) {
            //has enabled notify ==> disabling

            configuration.getEnabledNotifications().remove(player.getUniqueId());
            player.sendMessage("§7You will §cno longer §7receive notifications§8!");
        } else {
            //has disabled notify ==> enabling

            configuration.getEnabledNotifications().add(player.getUniqueId());
            player.sendMessage("§7You will §anow §7receive notifications§8!");
        }

        NotifyModule.getInstance().getController().getConfig().set(configuration);
        NotifyModule.getInstance().getController().getConfig().save();
    }

}
