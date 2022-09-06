package cloud.hytora.node.commands.impl;

import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.node.NodeDriver;

public class ClearCommand {

    @Command(
            label = "clear",
            desc = "Clears the screen",
            permission = "cloud.command.clear",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void executeClear(CommandContext<?> context, CommandArguments args) {
        NodeDriver.getInstance().getConsole().clearScreen();
        NodeDriver.getInstance().getConsole().printHeader();
    }
}
