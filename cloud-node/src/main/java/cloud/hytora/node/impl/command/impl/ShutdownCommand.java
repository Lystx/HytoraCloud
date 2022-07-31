package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;

import javax.annotation.Nonnull;

@Command({"shutdown", "exit", "end"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.command.use")
@CommandDescription("Stops the current Cloud-Instance")
public class ShutdownCommand {

    @Root
    public void onShutdown(@Nonnull CommandSender sender) throws Exception {
        NodeDriver.getInstance().shutdown();
    }

}
