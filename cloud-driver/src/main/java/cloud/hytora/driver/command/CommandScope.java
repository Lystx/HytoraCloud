package cloud.hytora.driver.command;

import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;

import javax.annotation.Nonnull;


public enum CommandScope {

	CONSOLE,
	CONSOLE_AND_INGAME,
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
		if (sender instanceof ConsoleCommandSender && isConsole()) return true;
		return false;
	}

}
