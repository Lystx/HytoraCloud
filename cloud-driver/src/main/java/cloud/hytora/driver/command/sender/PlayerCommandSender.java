package cloud.hytora.driver.command.sender;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.player.ICloudPlayer;

import javax.annotation.Nonnull;
import java.util.UUID;


/**
 * The {@link PlayerCommandSender} is based on the {@link CommandSender}
 * and defines that this sender is an Instance of {@link ICloudPlayer}
 *
 * Specific to this instance is that you can retrieve the provided {@link ICloudPlayer}
 * and the {@link UUID} of this player to interact and use in the API.
 *
 * @see CommandSender
 * @see Console
 *
 * @since DEV-1.0
 * @version SNAPSHOT-1.0
 * */
public interface PlayerCommandSender extends CommandSender {

	/**
	 * @return the {@link UUID} of this commandSender ({@link ICloudPlayer})
	 */
	@Nonnull
	UUID getUniqueId();

	/**
	 * @return the provided {@link ICloudPlayer} that is bound to this {@link PlayerCommandSender}
	 */
	@Nonnull
    ICloudPlayer getPlayer();


	/**
	 * Puts the cloud-prefix defined in {@link cloud.hytora.driver.common.CloudMessages}
	 * in front of the message.<br>
	 * To send a normal message use {@link cloud.hytora.driver.player.executor.PlayerExecutor#sendMessage(String)}
	 *
	 * @param message the message to send
	 */
	@Override
	void sendMessage(String message);
}
