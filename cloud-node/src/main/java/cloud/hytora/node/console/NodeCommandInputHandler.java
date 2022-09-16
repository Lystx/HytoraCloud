package cloud.hytora.node.console;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.defaults.ConsoleCommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.node.NodeDriver;

import java.util.function.Consumer;

public class NodeCommandInputHandler implements Consumer<String> {

    @Override
    public void accept(String s) {
        ICommandManager commandManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class);
        String[] args = s.split(" ");
        String command = args.length > 0 ? args[0] : s;

        if (!commandManager.hasCommand(command)) {

            DriverCommand similar = commandManager.getSimilarCommand(command);
            Logger.constantInstance().info((similar == null ?
                    "§cThis command is not known by the system! Use '§ehelp§c' for help." :
                    "§cWrong command! Did you mean '§e" + similar.getLabel() + "§c'?"));
        } else {
            Task.runAsync(() -> commandManager.executeCommand(args, new ConsoleCommandContext(NodeDriver.getInstance().getCommandSender())));
        }
    }
}
