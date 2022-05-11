package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.command.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface CommandCompleter {

	@Nonnull
	Collection<String> complete(@Nonnull CommandSender sender, @Nonnull String message, @Nonnull String argument);

}
