package cloud.hytora.modules.sign.cloud.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.sign.api.CloudSignAPI;

@Command(
        value = "signsReload",
        permission = "cloud.modules.sign.command.use",
        executionScope = CommandScope.CONSOLE,
        description = "reloads the sign config"

)
public class ModuleCloudSignCommand {

    @Command.Root
    public void handle(CommandSender sender) {

        CloudSignAPI.getInstance().publishConfiguration();
        CloudSignAPI.getInstance().getSignManager().update();
        sender.sendMessage("Reloaded!");
    }
}
