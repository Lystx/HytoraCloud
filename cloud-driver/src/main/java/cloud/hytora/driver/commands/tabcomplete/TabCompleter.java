package cloud.hytora.driver.commands.tabcomplete;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.reaction.GenericResult;
import lombok.Getter;
import cloud.hytora.driver.commands.parameter.AbstractBundledParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A reactor for the {@link TabCompletion} of a command execution
 */
@Getter
public class TabCompleter extends GenericResult<Collection<String>, TabCompleter> {

    /**
     * The parameter of the command execution
     */
    private final AbstractBundledParameters parameterSet;

    /**
     * The command itself
     */
    private final DriverCommand command;

    // sets the element
    {
        super.result = new ArrayList<>();
    }

    public TabCompleter(DriverCommand command, TabCompleteEvent event) {

        this.parameterSet = AbstractBundledParameters.newInstance(event.getParameter().subList(1, event.getParameter().size()));
        this.command = command;

        this.key = command.getPath();
        this.id = parameterSet.size();
    }

    @Override
    public TabCompleter getSelf() {
        return this;
    }

    public void reactWithSubCommands(String subCommand) {
        this.setResult(1,
                getCommand()
                        .getChildrens()
                        .stream()
                        .map(DriverCommand::getLabel)
                        .collect(Collectors.toList()),
                subCommand
        );
    }

    public void react(Collection<String> suggestions) {
        super.result = suggestions;
    }

    public void react(String... suggestions) {
        this.react(Arrays.asList(suggestions));
    }

}
