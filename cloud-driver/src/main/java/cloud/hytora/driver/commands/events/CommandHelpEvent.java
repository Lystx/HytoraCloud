package cloud.hytora.driver.commands.events;

import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.event.Cancelable;
import cloud.hytora.driver.event.CloudEvent;
import lombok.Getter;

/**
 * This event is for sending help about one specific command, that means the
 * user used the '-?' flag<br>
 * Cancelling this event means you've handled the event successfully
 */
@Getter
public class CommandHelpEvent<T extends CommandSender> implements CloudEvent, Cancelable {

    private CommandContext<T> context;
    private boolean cancelled = false;

    public CommandHelpEvent(CommandContext<T> context) {
        this.context = context;
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
