package cloud.hytora.driver.command;

import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;

import javax.annotation.Nonnull;


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

	CommandScope() {
		console = name().contains("CONSOLE");
		ingame = name().contains("INGAME");
	}

	public boolean isConsole() {
		return console;
	}

	public boolean isIngame() {
		return ingame;
	}

	public boolean isUniversal() {
		return this == CONSOLE_AND_INGAME;
	}

	public boolean covers(@Nonnull CommandSender sender) {
		if (sender instanceof PlayerCommandSender && isIngame()) return true;
		return sender instanceof ConsoleCommandSender && isConsole();
	}

}
