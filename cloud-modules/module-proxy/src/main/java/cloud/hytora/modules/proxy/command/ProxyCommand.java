package cloud.hytora.modules.proxy.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.proxy.ProxyModule;

@Command("proxy")
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.modules.proxy.command.use")
@CommandAutoHelp
@CommandDescription("Manages the proxy module")
public class ProxyCommand {

    @Command("rl")
    @CommandDescription("Reloads the proxy module")
    public void executeReload(CommandSender sender) {
        ProxyModule.getInstance().loadConfig();
        ProxyModule.getInstance().updateMotd();
        ProxyModule.getInstance().updateTabList();
        sender.sendMessage("Updated Module!");
    }
}
