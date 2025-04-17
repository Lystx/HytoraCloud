package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;

@Command(
        value = "clear",
        description = "Clears the console",
        executionScope = CommandScope.CONSOLE
)
@ApplicationParticipant
public class ClearCommand {

    @Command.Root
    public void executeClear(CommandSender sender) {
        NodeDriver.getInstance().getConsole().clearScreen();

    }
}
