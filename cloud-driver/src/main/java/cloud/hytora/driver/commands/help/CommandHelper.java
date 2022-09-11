package cloud.hytora.driver.commands.help;

import cloud.hytora.common.logging.formatter.SpacePadder;
import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.reaction.GenericResult;
import lombok.Getter;
import cloud.hytora.driver.commands.context.CommandContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A reactor for the {@link CommandHelp} during a command execution
 *
 * @param <T> The type of commandSender
 */
@Getter
public class CommandHelper<T extends CommandSender> extends GenericResult<Collection<String>, CommandHelper<T>> {

    /**
     * Parameter which can be used to specify the help more
     */
    private final Set<Object> parameter;

    /**
     * The context of the command execution
     */
    private final CommandContext<T> context;

    public CommandHelper(CommandContext<T> context, int id, Object... parameter) {
        super.result = new ArrayList<>();
        this.key = context.getCommand().getPath();
        this.id = id;

        this.context = context;
        this.parameter = new HashSet<>(Arrays.asList(parameter));
    }

    @Override
    public CommandHelper<T> getSelf() {
        return this;
    }

    /**
     * Sets the element of the reactable (another method name)
     *
     * @param messages The messages to be sent to the sender as help
     */
    public void setResult(String... messages) {
        this.result = Arrays.asList(messages);
    }


    public void performTemplateHelp(int... page) {
        int p = (page.length == 0) ? 1 : page[0];


        Collection<String> duplicates = new ArrayList<>();
        List<DriverCommand> commands = new ArrayList<>();

        for (DriverCommand command : context.getCommand().getChildrens().stream().sorted(Comparator.comparing(DriverCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getCommandScope().covers(context.getCommandSender())) {
                continue;
            }
            if (duplicates.stream().anyMatch(s -> command.getNames().contains(s))) { //to avoid aliases create conflicts
                continue;
            }
            duplicates.addAll(command.getNames());
            commands.add(command);

        }

        //split command help after 5 command entries
        List<List<DriverCommand>> splitCommands = CollectionUtils.splitCollection(commands, 10);
        try {
            List<DriverCommand> pagedCommands = splitCommands.get((p - 1));

            context.sendMessage("§8");
            context.sendMessage("§6=> SubCommands for '{}' §8[§b{}§8/§b{}§8]§8:", context.getCommand().getLabel(), p, splitCommands.size());
            context.sendMessage("§8");
            for (DriverCommand command : pagedCommands) {

                StringBuilder builder = new StringBuilder();

                int triggerLength = 22;
                int descriptionLength = 35;

                String triggers = command.getNames().toString();
                String description = (!command.getDescription().trim().isEmpty() ? command.getDescription() : "No Desc");

                if (triggers.length() > triggerLength) triggers = triggers.substring(triggers.length() - triggerLength);
                if (description.length() > descriptionLength) {
                    description = StringUtils.formatMessage("Desc too long » Use '{} {} -?' for help", context.getCommand().getLabel(), command.getLabel());
                }

                //command triggers
                builder.append("  §8» §7Trigger§8: §b");
                SpacePadder.padRight(builder, triggers, triggerLength);
                builder.append(" ");

                //command description
                builder.append("§8| §f");
                SpacePadder.padRight(builder, description, descriptionLength);

                context.sendMessage(builder.toString());
            }

            context.sendMessage("§8");
        } catch (Exception e) {
            context.sendMessage("§cThere is no page with index §e" + p + "§c!");
        }

    }
}
