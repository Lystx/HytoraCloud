package cloud.hytora.driver.command.sender.defaults;

import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;


@AllArgsConstructor @Getter
public class DefaultPlayerCommandSender implements PlayerCommandSender {

	private final CloudPlayer player;

	@Override
	public void sendMessage(@Nonnull String message) {
		// TODO: 11.04.2022  send messages to player
	}


	@Override
	public boolean hasPermission(@Nonnull String permission) {
		return false;
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
