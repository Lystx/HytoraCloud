package cloud.hytora.driver.command.sender;

import javax.annotation.Nonnull;

/**
 * @see PlayerCommandSender
 * @see ConsoleCommandSender
 */
public interface CommandSender {

	void sendMessage(@Nonnull String message);

	boolean hasPermission(@Nonnull String permission);

	@Nonnull
	String getName();

}
