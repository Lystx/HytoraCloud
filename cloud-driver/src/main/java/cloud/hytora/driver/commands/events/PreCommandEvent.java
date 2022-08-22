package cloud.hytora.driver.commands.events;

import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.event.Cancelable;
import cloud.hytora.driver.event.CloudEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;

/**
 * This events is called when during executing a command the system is walking down a level.
 * This happens even before the first level!
 * In this events you can check the permission, etc.
 *
 * @param <T>
 */
@Getter
public class PreCommandEvent<T extends CommandSender> implements CloudEvent, Cancelable {

    private CommandContext<T> context;
    private T commandSender;
    private DriverCommand newInstance;
    @Setter
    private ExecutorService service;

    private boolean cancelled;

    public PreCommandEvent(CommandContext<T> context, DriverCommand newInstance) {
        this.context = context;
        this.commandSender = context.getCommandSender();
        this.newInstance = newInstance;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
