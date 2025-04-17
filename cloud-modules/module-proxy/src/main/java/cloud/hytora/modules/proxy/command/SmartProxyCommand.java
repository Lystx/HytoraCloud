package cloud.hytora.modules.proxy.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.proxy.ProxyModule;

@Command(
        value = "proxy",
        permission = "cloud.modules.proxy.command.use",
        description = "Manages the proxy module",
        executionScope = CommandScope.CONSOLE_AND_INGAME
)
@Command.AutoHelp
public class SmartProxyCommand {

    @Command(value = "rl", description = "Reloads the proxy module")
    public void executeReload(CommandSender sender) {
        ProxyModule.getInstance().loadConfig();
        ProxyModule.getInstance().updateMotd();
        ProxyModule.getInstance().updateTabList();
        sender.sendMessage("Updated Module!");
    }
}
