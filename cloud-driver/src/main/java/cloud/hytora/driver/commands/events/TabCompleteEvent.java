package cloud.hytora.driver.commands.events;

import cloud.hytora.driver.commands.parameter.AbstractBundledParameters;
import cloud.hytora.driver.event.Cancelable;
import cloud.hytora.driver.event.CloudEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This event is fired when the user tries to tab complete inside execution of command
 */
@Getter
public class TabCompleteEvent implements CloudEvent, Cancelable {

    /**
     * The list of possible auto_completions
     */
    @Setter
    private Collection<String> suggestions = new ArrayList<>();

    /**
     * The full string (e.g. 'arg0 arg1 arg2')
     */
    private String cursor;

    /**
     * The parameter (e.g. 'arg0', 'arg1', 'arg2')
     */
    private List<String> parameter;

    /**
     * The size of arguments (empty also counts as one´)
     */
    private int argumentsSize;

    /**
     * The current string, that means the current argument the user is typing
     */
    private String currentBuffer;

    /**
     * The string before {@code currentBuffer}
     */
    private String beforeBuffer;

    /**
     * Is the event cancelled?
     */
    private boolean cancelled;

    public TabCompleteEvent(String cursor) {
        this.cursor = cursor;

        this.parameter = AbstractBundledParameters.retrieveArguments(cursor, true);
        this.argumentsSize = parameter.size();

        // the current buffer (where the cursor is)
        this.currentBuffer = parameter.get(argumentsSize - 1);

        // list every argument before current cursor index
        List<String> before = new ArrayList<>();
        if (argumentsSize != 1) {
            for (int i = 0; i < argumentsSize - 1; i++) {
                before.add(parameter.get(i));
            }
            before.add("");
        }
        this.beforeBuffer = argumentsSize == 1 ? "" : String.join(" ", before);
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
