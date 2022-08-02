package cloud.hytora.driver.command.sender;

import cloud.hytora.driver.player.ICloudPlayer;

import javax.annotation.Nonnull;
import java.util.UUID;


public interface PlayerCommandSender extends CommandSender {

	@Nonnull
	UUID getUniqueId();

	@Nonnull
    ICloudPlayer getPlayer();

}
