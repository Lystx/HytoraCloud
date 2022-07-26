package cloud.hytora.driver.command.sender;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.misc.StringUtils;

import javax.annotation.Nonnull;

/**
 * @see PlayerCommandSender
 * @see ConsoleCommandSender
 */
public interface CommandSender {

	default void forceMessage(@Nonnull String message) {

	}

	void sendMessage(String message);

	default void sendMessage(@Nonnull String message, Object... args) {
		sendMessage(DriverUtility.args(message, args));
	}

	boolean hasPermission(@Nonnull String permission);

	@Nonnull
	String getName();

}
