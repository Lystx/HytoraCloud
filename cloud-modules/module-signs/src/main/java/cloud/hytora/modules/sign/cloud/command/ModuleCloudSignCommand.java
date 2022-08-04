package cloud.hytora.modules.sign.cloud.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.modules.sign.api.CloudSignAPI;

@Command("signsReload")
@CommandPermission("cloud.modules.sign.command.use")
@CommandExecutionScope(CommandScope.CONSOLE)
@CommandDescription("reloads the sign config")
public class ModuleCloudSignCommand {

    @Root
    public void handle(CommandSender sender) {

        CloudSignAPI.getInstance().publishConfiguration();
        CloudSignAPI.getInstance().getSignManager().update();
        sender.sendMessage("Reloaded!");
    }
}
