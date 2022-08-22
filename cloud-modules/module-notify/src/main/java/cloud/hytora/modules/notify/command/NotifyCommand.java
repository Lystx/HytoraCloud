package cloud.hytora.modules.notify.command;

import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.sender.PlayerCommandSender;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

import static cloud.hytora.driver.commands.data.enums.AllowedCommandSender.BOTH;

@Command(
        label = "notify",
        desc = "Manages the notify module",
        scope = CommandScope.CONSOLE_AND_INGAME
)
public class NotifyCommand {


    @Command(
            parent = "notify",
            label = "toggle",
            desc = "Toggles receiving notifications",
            permission = "cloud.modules.notify.command.use"
    )
    public void toggleCommand(PlayerCommandContext ctx, CommandArguments args) {

        ctx.getPlayerAsync()
                .onTaskSucess(player -> {

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
                });

    }


}
