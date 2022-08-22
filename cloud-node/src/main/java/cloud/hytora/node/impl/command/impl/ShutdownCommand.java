package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.node.NodeDriver;

public class ShutdownCommand {

    @Command(
            label = "shutdown",
            desc = "Shuts down the Cloud!",
            aliases = {"exit", "stop", "end"},
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void execute(CommandContext<?> context, CommandArguments args) {
        NodeDriver.getInstance().shutdown();
    }

}
