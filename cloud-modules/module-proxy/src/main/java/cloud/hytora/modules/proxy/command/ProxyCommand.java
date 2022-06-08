package cloud.hytora.modules.proxy.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandAutoHelp;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.SubCommand;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.proxy.ProxyModule;

@Command(
        name = "proxy",
        permission = "cloud.command.use",
        scope = CommandScope.CONSOLE
)
@CommandAutoHelp
@CommandDescription("Manages the proxy module")
public class ProxyCommand {

    @SubCommand("rl")
    @CommandDescription("Reloads the proxy module")
    public void executeReload(CommandSender sender) {
        ProxyModule.getInstance().loadConfig();
        ProxyModule.getInstance().updateMotd();
        ProxyModule.getInstance().updateTabList();
        sender.sendMessage("Updated Module!");
    }
}
