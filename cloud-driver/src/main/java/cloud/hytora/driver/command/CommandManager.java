package cloud.hytora.driver.command;

import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public interface CommandManager {

	void setActive(boolean active);

	@CheckReturnValue
	boolean isActive();

	void executeCommand(@Nonnull CommandSender sender, @Nonnull String input);

	@Nonnull
	@CheckReturnValue
	Collection<String> completeCommand(@Nonnull CommandSender sender, @Nonnull String input);

	void registerCommand(@Nonnull Object command);

	void registerCommand(Class<?> commandClass);

	void setInActiveHandler(BiConsumer<CommandSender, String> handler);

	<T> void registerParser(Class<T> typeClass, ArgumentParser<T> parser);

	void unregisterCommand(@Nonnull Object command);

	void unregisterCommand(@Nonnull ClassLoader classLoader);

	void unregisterCommand(@Nonnull String name);

	@Nonnull
	@CheckReturnValue
	ConsoleCommandSender getThisSidesCommandSender();

	@Nonnull
	@CheckReturnValue
	Collection<RegisteredCommand> getCommands();

	@Nonnull
	@CheckReturnValue
	Map<Class<?>, ArgumentParser<?>> getParsers();

	@Nonnull
	@CheckReturnValue
    CommandCompleter getCompleter(@Nonnull Class<? extends CommandCompleter> completerClass);

}
