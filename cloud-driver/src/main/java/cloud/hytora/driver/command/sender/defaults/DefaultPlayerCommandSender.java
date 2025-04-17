package cloud.hytora.driver.command.sender.defaults;

import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;


@AllArgsConstructor @Getter
public class DefaultPlayerCommandSender implements PlayerCommandSender {

	/**
	 * the provided player for this sender
	 */
	private final ICloudPlayer player;

	@Override
	public void sendMessage(@Nonnull String message) {
		PlayerExecutor.forPlayer(player).sendMessage(message);
	}


	@Override
	public boolean hasPermission(@Nonnull String permission) {
		return player.hasPermission(permission);
	}

	@Nonnull
	@Override
	public String getName() {
		return player.getName();
	}

	@Nonnull
	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public String toString() {
		return "PlayerCommandSender[name=" + getName() + " uuid=" + getUniqueId() + "]";
	}

}
