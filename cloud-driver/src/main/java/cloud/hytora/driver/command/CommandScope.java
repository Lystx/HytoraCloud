package cloud.hytora.driver.command;

import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;


/**
 * A {@link CommandScope} defines where a certain {@link cloud.hytora.driver.command.annotation.Command}
 * is allowed to be executed or where a command is hosted
 *
 *
 */
@AllArgsConstructor
@Getter
public enum CommandScope {

	/**
	 * This command is only executable
	 * within the console
	 */
	CONSOLE,

	/**
	 * This command is a cloud command that is
	 * executable from console and ingame
	 *
	 * (Ingame you have to type "cloud <command>"
	 * so the system recognizes the "cloud" prefix
	 */
	CONSOLE_AND_INGAME,

	/**
	 * This command is only executable from ingame
	 * but the command is hosted on the cloud side
	 * (Not executable from console)
	 */
	INGAME_HOSTED_ON_CLOUD_SIDE,

	/**
	 * This command is only executable from ingame
	 * and is also hosted ingame
	 */
	INGAME;

	private final boolean console, ingame;

	/**
	 * Sets the default values
	 */
	CommandScope() {
		console = name().contains("CONSOLE");
		ingame = name().contains("INGAME");
	}

	/**
	 * @return if command can be executed ingame and console-sided
	 */
	public boolean isUniversal() {
		return this == CONSOLE_AND_INGAME;
	}

	/**
	 * Checks if the given {@link CommandSender} covers this {@link CommandScope}
	 *
	 * @param sender the sender to check
	 * @return true or false
	 */
	public boolean covers(@Nonnull CommandSender sender) {
		if (sender instanceof PlayerCommandSender && isIngame()) return true;
		return sender instanceof ConsoleCommandSender && isConsole();
	}

}
