package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.SubCommand;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;

import javax.annotation.Nonnull;

@Command(
        name = {"stop", "shutdown", "end", "exit"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandDescription("Stops the current Cloud-Instance")
public class ShutdownCommand {

    @SubCommand("")
    public void onShutdown(@Nonnull CommandSender sender) throws Exception {
        NodeDriver.getInstance().shutdown();
    }

}
