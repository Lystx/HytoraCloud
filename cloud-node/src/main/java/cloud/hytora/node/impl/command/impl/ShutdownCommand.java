package cloud.hytora.node.impl.command.impl;


import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;

import javax.annotation.Nonnull;

@Command(
        value = {"shutdown", "exit", "end"},
        permission = "cloud.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Stops the current Cloud-Instance"
)
@ApplicationParticipant
public class ShutdownCommand {

    @Command.Root
    public void onShutdown(@Nonnull CommandSender sender) throws Exception {
        NodeDriver.getInstance().shutdown();
    }

}
