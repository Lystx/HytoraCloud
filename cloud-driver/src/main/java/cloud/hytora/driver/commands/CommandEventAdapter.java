package cloud.hytora.driver.commands;

import cloud.hytora.driver.commands.events.CommandErrorEvent;
import cloud.hytora.driver.commands.events.CommandHelpEvent;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.event.EventListener;

/**
 * This class is for implementing all important command listener methods<br>
 * Includes: {@link TabCompleteEvent}, {@link CommandErrorEvent}, {@link CommandHelpEvent}
 *
 * @param <T> The type of the command context
 */
public abstract class CommandEventAdapter<T extends CommandSender>  {

    @EventListener
    public void tabComplete(TabCompleteEvent event) {
        onTabComplete(event);
    }

    /**
     * This event is for getting possibilities for completing an argument
     *
     * @param event The event
     */
    public abstract void onTabComplete(TabCompleteEvent event);

    /**
     * This event is for handling an error during the command process
     *
     * @param event The event
     */
    public abstract void onCommandError(CommandErrorEvent<T> event);

    @EventListener
    public void commandHelp(CommandHelpEvent<T> event) {
        onCommandHelp(event);
    }

    @EventListener
    public void commandError(CommandErrorEvent<T> event) {
        onCommandError(event);
    }

    /**
     * This event is for handling the help flag '-?' during the command process
     *
     * @param event The event
     */
    public abstract void onCommandHelp(CommandHelpEvent<T> event);


}
