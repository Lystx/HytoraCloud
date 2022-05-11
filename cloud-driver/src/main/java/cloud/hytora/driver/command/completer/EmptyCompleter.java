package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.command.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;


public class EmptyCompleter implements CommandCompleter {

	@Nonnull
	@Override
	public Collection<String> complete(@Nonnull CommandSender sender, @Nonnull String message, @Nonnull String argument) {
		return Collections.emptyList();
	}
}
