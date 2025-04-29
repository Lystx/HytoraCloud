package cloud.hytora.remote.impl;


import cloud.hytora.driver.command.DefaultCommandManager;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteCommandManager extends DefaultCommandManager {

	public RemoteCommandManager() {
		this.setActive(true);
	}

	@Override
	protected void handleCommandChange() {
	}

	@Override
	public @NotNull ConsoleCommandSender getThisSidesCommandSender() {
		return (ConsoleCommandSender) Remote.getInstance().getCommandSender();
	}
}
