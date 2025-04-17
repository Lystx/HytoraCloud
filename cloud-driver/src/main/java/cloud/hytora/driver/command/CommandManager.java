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

/**
 *
 * The {@link CommandManager} is an essential Manager in the CloudNetwork.
 * It manages and handles all the registered and executed Commands.
 * As on Node-Side also on Ingame-Side.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version SNAPSHOT-1.5
 */
public interface CommandManager {

	/**
	 * Activates the CommandManager and start/stop listening for commands
	 *
	 * @param active if active or not
	 */
	void setActive(boolean active);

	/**
	 * @return if command manager is active
	 */
	@CheckReturnValue
	boolean isActive();

	/**
	 * Main-Method to execute a command for the given {@link CommandSender}
	 *
	 * @param sender the sender that executes
	 * @param input the input that was given
	 */
	void executeCommand(@Nonnull CommandSender sender, @Nonnull String input);

	/**
	 * Searches for completions for given input at command-index.
	 *
	 * @param sender the sender that executes
	 * @param input the input that was given (specific argument)
	 * @return collection of completions as {@link String}
	 */
	@Nonnull
	@CheckReturnValue
	Collection<String> completeCommand(@Nonnull CommandSender sender, @Nonnull String input);

	/**
	 * Registers a Command-Object
	 *
	 * @param command the object
	 */
	void registerCommand(@Nonnull Object command);

	/**
	 * Registers a Command based on the Class.
	 *
	 * @param commandClass the class
	 */
	void registerCommand(Class<?> commandClass);

	/**
	 * Sets the handler that runs when {@link CommandManager#setActive(boolean)} is called
	 *
	 * @param handler the handler to run
	 */
	void setInActiveHandler(BiConsumer<CommandSender, String> handler);

	/**
	 * Registers {@link ArgumentParser}s for this manager
	 *
	 * @param typeClass the class of the parsing object
	 * @param parser the parser
	 * @param <T> the object type
	 */
	<T> void registerParser(Class<T> typeClass, ArgumentParser<T> parser);

	/**
	 * Unregisters a command object directly
	 *
	 * @param command the object
	 */
	void unregisterCommand(@Nonnull Object command);

	/**
	 * Unregisters all commands that were found within a {@link ClassLoader}
	 *
	 * @param classLoader the class-loader
	 */
	void unregisterCommand(@Nonnull ClassLoader classLoader);

	/**
	 * Unregisters a command by its name.
	 * Aliases are also possible to use.
	 *
	 * @param name the name or alias of the command
	 */
	void unregisterCommand(@Nonnull String name);

	/**
	 * Returns this side {@link ConsoleCommandSender}
	 */
	@Nonnull
	@CheckReturnValue
	ConsoleCommandSender getThisSidesCommandSender();

	/**
	 * @return all cached and registered {@link RegisteredCommand}
	 */
	@Nonnull
	@CheckReturnValue
	Collection<RegisteredCommand> getCommands();

	/**
	 * @return all cached and registered {@link ArgumentParser}
	 */
	@Nonnull
	@CheckReturnValue
	Map<Class<?>, ArgumentParser<?>> getParsers();

	/**
	 * @param completerClass the class of the completer
	 * @return the completer of the class
	 */
	@Nonnull
	@CheckReturnValue
    CommandCompleter getCompleter(@Nonnull Class<? extends CommandCompleter> completerClass);

}
