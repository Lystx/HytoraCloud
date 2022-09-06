package cloud.hytora.modules.proxy.command;

import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.modules.proxy.ProxyModule;

@Command(
        label = "proxy",
        scope = CommandScope.CONSOLE_AND_INGAME,
        desc = "Manages the proxy module",
        permission = "cloud.command.proxy",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)
public class ProxyCommand {

    @Command(
            parent = "proxy",
            label = "rl",
            desc = "Reloads the proxy module",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void executeReload(CommandContext<?> ctx, CommandArguments args) {
        ProxyModule.getInstance().loadConfig();
        ProxyModule.getInstance().updateMotd();
        ProxyModule.getInstance().updateTabList();
        ctx.sendMessage("Updated Module!");
    }
}
