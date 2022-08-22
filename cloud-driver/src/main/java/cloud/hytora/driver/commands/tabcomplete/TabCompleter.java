package cloud.hytora.driver.commands.tabcomplete;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.reaction.Reactable;
import lombok.Getter;
import cloud.hytora.driver.commands.parameter.AbstractBundledParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * A reactor for the {@link TabCompletion} of a command execution
 */
@Getter
public class TabCompleter extends Reactable<List<String>> {

    /**
     * The parameter of the command execution
     */
    private AbstractBundledParameters parameterSet;

    /**
     * The command itself
     */
    private DriverCommand command;

    // sets the element
    {
        super.element = new ArrayList<>();
    }

    public TabCompleter(DriverCommand command, TabCompleteEvent event) {
        this.parameterSet = AbstractBundledParameters.newInstance(event.getParameter().subList(1, event.getParameter().size()));
        this.command = command;

        this.key = command.getPath();
        this.id = parameterSet.size();
    }

    /**
     * React with the subcommands of the tabCompletor's command
     *
     * @param keys The keys
     */
    public void reactWithSubCommands(String... keys) {
        react(1, StringUtils.getStringList(getCommand().getChildrens(),
                DriverCommand::getLabel
        ), keys);
    }

    /**
     * React to the before argument (shifted 'index' to the left) if it is a flag
     *
     * @param flag      The flag without '-'
     * @param index     The index to be shifted
     * @param procedure The procedure
     * @param keys      The keys
     */
    public void react(String flag, int index, Runnable procedure, String... keys) {
        String argumentToCheck = parameterSet.getBefore(index);
        if(argumentToCheck == null) return;

        // if the key is correct AND the argument is correct like '-flag'
        if((keys.length == 0 || checkKey(keys))
                && argumentToCheck.equalsIgnoreCase("-" + flag)) {
            procedure.run();
        }
    }

    public void react(String flag, int index, List<String> elements, String... keys) {
        react(flag, index, () -> setSuggestions(elements), keys);
    }

    public void react(String flag, Runnable procedure, String... keys) {
        react(flag, 1, procedure, keys);
    }

    public void react(String flag, List<String> elements, String... keys) {
        react(flag, 1, elements, keys);
    }

    public void setSuggestions(List<String> l) {
        this.element = l;
    }

}
