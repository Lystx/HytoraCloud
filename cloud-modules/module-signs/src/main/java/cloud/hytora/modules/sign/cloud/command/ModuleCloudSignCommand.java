package cloud.hytora.modules.sign.cloud.command;

import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.modules.sign.api.CloudSignAPI;

public class ModuleCloudSignCommand {

    @Command(
            label = "signsRl",
            permission = "cloud.modules.sign.command.use",
            scope = CommandScope.CONSOLE
    )
    public void reload(CommandContext<?> ctx, CommandArguments args) {

        CloudSignAPI.getInstance().publishConfiguration();
        CloudSignAPI.getInstance().getSignManager().update();
        ctx.sendMessage("Reloaded!");
    }
}
