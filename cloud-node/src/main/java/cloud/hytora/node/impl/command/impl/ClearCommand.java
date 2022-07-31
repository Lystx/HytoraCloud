package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandExecutionScope;
import cloud.hytora.driver.command.annotation.Root;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;

@Command("clear")
@CommandExecutionScope(CommandScope.CONSOLE)
@CommandDescription("Clears the console")
public class ClearCommand {

    @Root
    public void executeClear(CommandSender sender) {
        NodeDriver.getInstance().getConsole().clearScreen();

    }
}
