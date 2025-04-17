package cloud.hytora.driver.command.sender;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.misc.StringUtils;

import javax.annotation.Nonnull;

/**
 * A {@link CommandSender} is a Cloud-Entity that can interact with messages,
 * execute commands and check for certain permissions
 * It is used all over the CloudSystem to exchange informations
 *
 *
 * @see PlayerCommandSender
 * @see ConsoleCommandSender
 *
 * @since DEV-1.0
 * @version SNAPSHOT-1.0
 * @author Lystx
 */
public interface CommandSender {

	default void forceMessage(@Nonnull String message) {

	}

	/**
	 * Sends a message to this CommandSender
	 *
	 * IF {@link cloud.hytora.driver.command.Console} -> Displays it in consolöe
	 * If {@link cloud.hytora.driver.player.ICloudPlayer} -> Sends packet to send message
	 *
	 * @param message the message to display
	 */
	void sendMessage(String message);

	/**
	 * Sends a message like in {@link #sendMessage(String)} but it formats and replaces '{}' with given arguments
	 *
	 * @param message the message (that contains parameters)
	 * @param args the arguments to replace
	 */
	default void sendMessage(@Nonnull String message, Object... args) {
		sendMessage(DriverUtility.args(message, args));
	}

	/**
	 * Checks if this commandSender has certain permissions to execute commands
	 *
	 * @param permission the permission to check
	 * @return true or false
	 */
	boolean hasPermission(@Nonnull String permission);

	/**
	 * @return the name of the sender
	 */
	@Nonnull
	String getName();

}
