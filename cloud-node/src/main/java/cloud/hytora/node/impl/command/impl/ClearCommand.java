package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.node.NodeDriver;

public class ClearCommand {

    @Command(
            label = "clear",
            desc = "Clears the console",
            aliases = {"cls"}
    )
    public void executeClear(CommandContext<?> context, CommandArguments args) {
        NodeDriver.getInstance().getConsole().clearScreen();
        NodeDriver.getInstance().getConsole().printHeader();
    }
}
