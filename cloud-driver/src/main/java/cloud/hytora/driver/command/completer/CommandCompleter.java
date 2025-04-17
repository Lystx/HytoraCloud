package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.command.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This interface displays a completer for commands
 * when the console-user presses TAB.
 *
 * This is integrated into {@link cloud.hytora.driver.command.annotation.Command.Argument}
 */
public interface CommandCompleter {

	/**
	 * The complete method where the given possible answers are given
	 *
	 * @param sender the sender that requests the completes
	 * @param argument the argument that needs completion
	 * @return a collection of completions to display
	 */
	@Nonnull
	Collection<String> complete(@Nonnull CommandSender sender, @Nonnull String argument);

}
