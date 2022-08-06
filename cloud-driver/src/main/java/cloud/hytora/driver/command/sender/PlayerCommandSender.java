package cloud.hytora.driver.command.sender;

import cloud.hytora.driver.player.ICloudPlayer;

import javax.annotation.Nonnull;
import java.util.UUID;


public interface PlayerCommandSender extends CommandSender {

	@Nonnull
	UUID getUniqueId();

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
