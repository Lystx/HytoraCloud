package cloud.hytora.driver.commands;

import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.parameter.IParameterTypeRegistry;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.common.IRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public interface ICommandManager extends IRegistry<String, DriverCommand> {

    /**
     * The parameter type registry
     */
    IParameterTypeRegistry getParamTypeRegistry();

    /**
     * Registers an eventAdapter which means registering its events
     *
     * @param eventAdapter The event adapter
     * @param <T>          The type
     */
    public <T extends CommandSender> void registerEventAdapter(CommandEventAdapter<T> eventAdapter);

    /**
     * Executes a command
     *
     * @param args    The arguments
     * @param context The context
     * @param <T>     The type
     */
    public <T extends CommandSender> boolean executeCommand(String[] args, CommandContext<T> context);

    /**
     * Gets the command with given label
     *
     * @param label The label
     * @return The command
     */
    public DriverCommand getCommand(String label);

    /**
     * Gets the most similar command from given label
     *
     * @param label The label
     * @return The command instance
     */
    public DriverCommand getSimilarCommand(String label);

    /**
     * Checks if the registry contains a command with given label
     *
     * @param label The label
     * @return The result
     */
    boolean hasCommand(String label);

    /**
     * Get all registered commands
     *
     * @return The map of commands
     */
    public Map<String, DriverCommand> getRegisteredCommands();

    /**
     * Get the commands labels
     *
     * @return The commands labels
     */
    public List<String> getCommands();

    /**
     * Get the commands with the command type root
     *
     * @return The list of commands
     */
    public List<DriverCommand> getRootCommands();

    void setActive(boolean active);

    void setActive(boolean active, BiConsumer<CommandSender, String> inActiveHandler);

    @Nullable
    BiConsumer<CommandSender, String> getInactiveHandler();

    boolean isActive();

    /**
     * Clears all maps
     */
    public void unregisterAll();

    /**
     * Register commands from given classes
     *
     * @param classes The classes
     */
    public int registerCommands(Object... classes);

    public int registerCommandsSeperately(Object... classes);

    void unregister(Class<?> cmdClass);

}
