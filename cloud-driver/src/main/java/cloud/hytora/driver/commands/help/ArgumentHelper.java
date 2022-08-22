package cloud.hytora.driver.commands.help;

import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.reaction.Reactable;
import lombok.Getter;
import cloud.hytora.driver.commands.context.CommandContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A reactor for the {@link ArgumentHelp} during a command execution
 *
 * @param <T> The type of commandSender
 */
@Getter
public class ArgumentHelper<T extends CommandSender> extends Reactable<Collection<String>> {

    /**
     * Parameter which can be used to specify the help more
     */
    private Set<Object> parameter;

    /**
     * The context of the command execution
     */
    private CommandContext<T> context;

    // sets the element of the parent
    {
        super.element = new ArrayList<>();
    }

    public ArgumentHelper(CommandContext<T> context, int id, Object... parameter) {
        this.key = context.getCommand().getPath();
        this.id = id;

        this.context = context;
        this.parameter = new HashSet<>(Arrays.asList(parameter));
    }

    /**
     * Gets a parameter from {@link #parameter} only by using the parameter's class
     *
     * @param eClass The parameter class
     * @param <E>    The generic type of the class
     * @return The parameter object
     */
    public <E> E getParam(Class<E> eClass) {
        for(Object param : parameter) {
            if(param != null && eClass.isAssignableFrom(param.getClass())) {
                return (E) param;
            }
        }
        return null;
    }

    /**
     * Sets the element of the reactable (another method name)
     *
     * @param messages The messages to be sent to the sender as help
     */
    public void setHelpMessages(String... messages) {
        this.element = Arrays.asList(messages);
    }


    public void performTemplateHelp() {

        context.sendMessage("§8");
        context.sendMessage("§7SubCommands for '" + context.getCommand().getLabel() + "'§8:");


        for (DriverCommand command : context.getCommand().getChildrens().stream().sorted(Comparator.comparing(DriverCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getCommandScope().covers(context.getCommandSender())) {
                continue;
            }

            List<String> aliases = command.getAliases();
            String aliasString = aliases.isEmpty() ? "" : " §8(§b" + String.join("§7, " + "§b", (aliases.isEmpty() ? "§c/" : aliases.toString()).replace("[", "").replace("]", "") + "§8)");
            context.sendMessage("§b" + context.getCommand().getLabel() + " " + (command.getLabel() + " " + command.getUsage().getBase()) + aliasString + " §8× §f" + (!command.getDescription().trim().isEmpty() ? command.getDescription() : "No Description"));
        }
        context.sendMessage("§8");
    }
}
