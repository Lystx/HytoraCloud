package cloud.hytora.driver.command.sender;

import cloud.hytora.driver.player.CloudPlayer;

import javax.annotation.Nonnull;
import java.util.UUID;


public interface PlayerCommandSender extends CommandSender {

	@Nonnull
	UUID getUniqueId();

	@Nonnull
    CloudPlayer getPlayer();

}
