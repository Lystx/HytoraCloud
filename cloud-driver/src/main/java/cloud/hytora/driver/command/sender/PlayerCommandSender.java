package cloud.hytora.driver.command.sender;

import cloud.hytora.driver.command.console.Console;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.entity.player.CloudPlayer;

import javax.annotation.Nonnull;
import java.util.UUID;


/**
 * The {@link PlayerCommandSender} is based on the {@link CommandSender}
 * and defines that this sender is an Instance of {@link CloudPlayer}
 *
 * Specific to this instance is that you can retrieve the provided {@link CloudPlayer}
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
	 * @return the {@link UUID} of this commandSender ({@link CloudPlayer})
	 */
	@Nonnull
	UUID getUniqueId();

	/**
	 * @return the provided {@link CloudPlayer} that is bound to this {@link PlayerCommandSender}
	 */
	@Nonnull
    CloudPlayer getPlayer();


	/**
	 * Puts the cloud-prefix defined in {@link UniversalCloudMessages}
	 * in front of the message.<br>
	 * To send a normal message use {@link CloudPlayer#sendMessage(String)}
	 *
	 * @param message the message to send
	 */
	@Override
	void sendMessage(String message);
}
