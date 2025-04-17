package cloud.hytora.driver.command.sender;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.Console;

import javax.annotation.Nonnull;

/**
 * The {@link ConsoleCommandSender} is based on the {@link CommandSender}
 * and defines that this sender is an Instance of {@link Console}
 *
 * Specific to this instance is that you can retrieve the provided {@link Console}
 * and the {@link Logger} instance to display and interact.
 *
 * @see CommandSender
 * @see Console
 *
 * @since DEV-1.0
 * @version SNAPSHOT-1.0
 * */
public interface ConsoleCommandSender extends CommandSender {

    /**
     * @return the {@link Console} instance of this sender
     */
	@Nonnull
    Console getConsole();

    /**
     * @return the {@link Logger} instance of this sender
     */
	@Nonnull
    Logger getLogger();

}
