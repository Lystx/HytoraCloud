package cloud.hytora.modules.smartproxy.commands;

import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.smartproxy.SmartProxyModule;

@Command(
        value = "smartproxy",
        permission = "cloud.modules.smartproxy.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages the smartproxy module"
)
@Command.AutoHelp
public class SmartProxyCommand {


    @Command(value = "toggle", description = "Enables or disables the smartproxy module")
    public void executeReload(CommandSender sender) {

        StorableDocument config = SmartProxyModule.getInstance().getController().getConfig();
        boolean toggle = !config.getBoolean("enabled");
        config.set("enabled", toggle);
        config.save();
        SmartProxyModule.getInstance().setEnabled(toggle);
        sender.sendMessage("§7SmartProxy-System is now " + (toggle ? "§aenabled" : "§cdisabled") + "§h!");
    }


    @Command(value = "setMode", description = "Verändert den ProxySuch Modus")
    @Command.Syntax("<mode>")
    public void executeSetMode(CommandSender sender, @Command.Argument("mode") String mode) {

        StorableDocument config = SmartProxyModule.getInstance().getController().getConfig();
        if (mode.equalsIgnoreCase("RANDOM")) {
            mode = "FILL";
        } else if (mode.equalsIgnoreCase("FILL")) {
            mode = "BALANCED";
        } else {
            mode = "RANDOM";
        }

        config.set("proxySearchMode", mode);
        config.save();
        SmartProxyModule.getInstance().setProxySearchMode(mode);
        sender.sendMessage("SmartProxy-System now searches for free proxies with §e" + mode.toUpperCase() + "-Mode§h!");
        return;
    }
}
