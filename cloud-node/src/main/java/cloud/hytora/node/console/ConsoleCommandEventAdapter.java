package cloud.hytora.node.console;

import cloud.hytora.common.collection.pair.Pair;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.CommandEventAdapter;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.events.CommandErrorEvent;
import cloud.hytora.driver.commands.events.CommandHelpEvent;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.commands.exceptions.CommandException;
import cloud.hytora.driver.commands.exceptions.InvalidCommandUsageException;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;

public class ConsoleCommandEventAdapter extends CommandEventAdapter {

    @Override
    public void onTabComplete(TabCompleteEvent event) {
        ICommandManager commandManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class);
        if(event.getArgumentsSize() == 1) {
            event.setSuggestions(commandManager.getCommands());
            return;
        }

        String firstArg = event.getParameter().get(0);
        DriverCommand instance = commandManager.getCommand(firstArg);
        Pair<DriverCommand, String[]> context
                = instance.getInstance(event.getParameter().subList(1, event.getParameter().size())
                .toArray(new String[]{}), null);
        if(instance == null || context == null) {
            return;
        }
        instance = context.getFirst();

        TabCompleter completor = new TabCompleter(instance, event);
        instance.executeTabCompletion(completor);
        event.setSuggestions(completor.getResult());
    }

    @Override
    public void onCommandError(CommandErrorEvent event) {
        Throwable exception = event.getException();
        CommandContext context = event.getContext();

        if(exception instanceof CommandException) {
            CommandException e = (CommandException) exception;
            context.sendMessage("§c" + e.getType().getMessage(e.getReplacements()));
        } else if(exception instanceof InvalidCommandUsageException) {
            InvalidCommandUsageException e = (InvalidCommandUsageException) exception;

            if(e.getType() == InvalidCommandUsageException.Type.NOT_ALLOWED) {
                context.sendMessage("§4You are not allowed to execute this command!");
                return;
            }

            context.sendUsage("§cUsage: §e", true);
        }
        else {
            context.sendMessage("§4Error while executing the command (" + exception.getClass().getSimpleName() + ")!");
            System.err.println("Error while executing command: ");
            exception.printStackTrace();
        }
    }

    @Override
    public void onCommandHelp(CommandHelpEvent event) {
        CommandContext context = event.getContext();
        DriverCommand instance = context.getCommand();


        context.sendMessage("§8");
        context.sendMessage("§6=> CommandHelp for '{}'§8:", instance.getLabel());
        context.sendMessage("§8");
        if (!event.isOnlyDescription()) {
            context.sendMessage("  §8» §7Syntax§8: §b{} {}", instance.getLabel(), instance.getUsage().getBase());
            context.sendMessage("  §8» §7Aliases§8: §b{}", instance.getAliases());
            context.sendMessage("  §8» §7Scope§8: §b{}", instance.getCommandScope());
            context.sendMessage("  §8» §7Flags§8: §b{}", instance.getFlags());
            if (instance.getMethod() == null) {
                context.sendMessage("  §8» §7Source§8: §bParent-With-SubCommands");
            } else {
                context.sendMessage("  §8» §7Source§8: §b{}",instance.getMethod().getDeclaringClass().getSimpleName() + "#" + instance.getMethod().getName() + "()");
            }
        }
        context.sendMessage("  §8» §7Description§8: §b{}", instance.getDescription());
        context.sendMessage("§8");
        event.setCancelled(true);
    }
}
