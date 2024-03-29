package cloud.hytora.driver.commands.context;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.util.Validation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.events.CommandErrorEvent;
import cloud.hytora.driver.commands.exceptions.InvalidCommandUsageException;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.common.util.DisplayFormat;
import cloud.hytora.common.util.MultiCache;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.event.IEventManager;
import lombok.Getter;
import lombok.Setter;
import net.jodah.expiringmap.ExpiringMap;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The context of the command means a wrapper for the command itself, the command sender and the
 * arguments
 *
 * @param <T> The type of commandSender (different for bungee/spigot)
 */
@Getter
public abstract class CommandContext<T extends CommandSender> {

    /**
     * The name of the console
     */
    public static final String CONSOLE_NAME = ".CONSOLE";

    /**
     * The unique id if the commandSender is the console
     */
    public static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes(CONSOLE_NAME.getBytes());

    /**
     * This cache is for storing values during executing commands
     */
    private static final MultiCache<UUID, String, Object> CONTEXT_CACHE = new MultiCache<>();

    /**
     * Sender of the command (can be null)
     */
    private T commandSender;

    /**
     * The instance of the command
     */
    @Setter
    private DriverCommand command;

    /**
     * The parameter of the command
     */
    @Setter
    private CommandArguments paramSet;

    /**
     * The custom properties
     */
    private final Map<String, Object> properties;

    public CommandContext(T commandSender) {
        this.commandSender = commandSender;
        this.properties = new HashMap<>();
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public <V> V getProperty(String key) {
        return (V) properties.get(key);
    }

    /**
     * Gets the unique id of the sender, that means either the {@link #CONSOLE_UUID} or the uuid of the player
     *
     * @return The uuid
     */
    public abstract UUID getSendersUniqueId();

    /**
     * Sends a message to given target
     *
     * @param target The target
     * @param msg    The message
     */
    protected abstract void message(String msg, T target);

    /**
     * Sends a display format to the receivers
     *
     * @param format    The format
     * @param receivers The receivers
     */
    public abstract void sendDisplayFormat(DisplayFormat format, T... receivers);

    /**
     * Checks if the sender is the console
     *
     * @return The result
     */
    public boolean isConsole() {
        return getSendersUniqueId().equals(CONSOLE_UUID);
    }

    /**
     * Sends a message to all targets or to the commandSender
     *
     * @param msg          The msg
     * @param replacements The replacements for the message
     * @return The message context
     */
    public MessageContextResult<T> sendMessage(String msg, Object... replacements) {
        String fullMessage = StringUtils.formatMessage(msg, replacements);

        // there is a hover/click event thingy inside!
        // AND if it is the console we cannot do!
        if (Validation.MESSAGE_COMP_EVENT.matches(msg) && isConsole()) {
            return new MessageContextResult<>(this, msg, false, true);
        }

        // OTHERWISE can do!
        message(fullMessage, commandSender);
        return new MessageContextResult<>(this, msg, true, true);
    }

    public void sendMessage(List<String> messages) {
        messages.forEach(s -> sendMessage(s));
    }

    /**
     * Same as {@link #sendMessage(String, Object...)} but with a condition to be fullfilled
     *
     * @param condition    The condition
     * @param msg          The message
     * @param replacements The replacements for the message
     * @return The message context
     */
    public MessageContextResult<T> sendMessage(boolean condition, String msg, Object... replacements) {
        if (!condition) return new MessageContextResult<>(this, msg, false, true);
        return sendMessage(msg, replacements);
    }

    public MessageContextResult<T> sendMessage(boolean condition) {
        if (!condition) return new MessageContextResult<>(this, null, false, true);
        return new MessageContextResult<>(this, null, true, true);
    }

    /**
     * Sends a message only to the command sender
     *
     * @param msg The message as string
     */
    public void sendMessages(String... msg) {
        sendMessage(Arrays.asList(msg));
    }

    /**
     * Throws an invalid usage exception and therefore triggers the argument helper
     *
     * @see InvalidCommandUsageException
     * @see CommandHelper
     */
    public void invalidUsage(Object... params) {
        CloudDriver
                .getInstance()
                .getProviderRegistry()
                .getUnchecked(IEventManager.class)
                .callEventOnlyLocally(
                        new CommandErrorEvent<>(
                                this,
                                command,
                                new InvalidCommandUsageException(InvalidCommandUsageException.Type.CUSTOM_EVENTABLE, command, params)
                        )
                );


    }


    /**
     * Executes the argument helper with current argument and given params
     *
     * @param params The parameter
     */
    public boolean sendHelp(Object... params) {
        return sendHelpCurrent(false, params);
    }

    public boolean sendHelpCurrent(boolean before, Object... params) {
        CommandHelper<T> helper = new CommandHelper<>(this, getParamSet().size() + (before ? -1 : 0), params);
        command.executeArgumentHelper(helper);
        boolean r = false;

        for (String s : helper.getResult()) {
            sendMessage(s);

            if (!r) r = true;
        }
        return r;
    }

    /**
     * Sends the usage to the commandSender
     *
     * @param prefix The prefix (e.g. a color or whatever)
     */
    public void sendUsage(String prefix, boolean highlighted) {
        sendMessage(prefix + getUsage(highlighted, false));
    }

    public void sendUsage(String prefix) {
        sendUsage(prefix, false);
    }

    public void sendUsage() {
        sendUsage("");
    }

    /**
     * Similar to {@link #sendUsage(String, boolean)}<br>
     * Current means the param he recently entered
     *
     * @param prefix      The prefix
     * @param highlighted Highlighted the current param
     */
    public void sendCurrentUsage(String prefix, boolean highlighted) {
        sendMessage(prefix + getUsage(highlighted, true));
    }

    public void sendCurrentUsage(String prefix) {
        sendCurrentUsage(prefix, true);
    }

    public void sendCurrentUsage() {
        sendCurrentUsage("");
    }

    /**
     * Gets the current argument of the usage with using the parameter size
     *
     * @param before Before means either ('1 2 3'=3 or '1 2 3'=2)
     * @return The key
     */
    public String getCurrentUsageArg(boolean before) {
        int size = getParamSet().size();
        String param = command.getUsage().getParam(size - (before ? 1 : 0));
        if (param == null) return "";
        return param;
    }

    /**
     * Gets the usage of the inherited command
     *
     * @return The usage as string
     */
    public String getUsage(boolean highlighted, boolean current) {
        if (command == null) return "";

        //check if '/' is needed on current cloud environment
        String usage = (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.SERVICE ? "/" : "");
        usage = (highlighted ? "§7" : "") + usage + command.getPath().replace(".", " ");
        usage += " " + command.getUsage().getBase();

        // list current arg
        String currentArg = getCurrentUsageArg(current);
        usage = usage.replace(currentArg, "§c" + currentArg + "§7");

        return usage;
    }

    public String getUsage() {
        return getUsage(false, false);
    }

    /**
     * Stores a value with the key temporarily
     *
     * @param key              The key
     * @param value            The value
     * @param policy           The policy of the expiration
     * @param duration         The duration
     * @param unit             The unit
     * @param removalListeners The listeners if the value is removed
     */
    public void set(String key, Object value, ExpiringMap.ExpirationPolicy policy, long duration, TimeUnit unit, Consumer... removalListeners) {
        UUID uuid = getSendersUniqueId();
        String realKey = getCommand().getLabel() + ":" + key;

        CONTEXT_CACHE.put(uuid, realKey, value, policy, duration, unit, removalListeners);
    }

    public void set(Object value, ExpiringMap.ExpirationPolicy policy, long duration, TimeUnit unit, Consumer... removalListeners) {
        this.set(value.getClass().getName(), value, policy, duration, unit, removalListeners);
    }

    public void set(String key, Object value, Consumer... removalListeners) {
        this.set(key, value, null, -1, null, removalListeners);
    }

    public void set(Object value, Consumer... removalListeners) {
        this.set(value, null, -1, null, removalListeners);
    }

    /**
     * Similar function as {@link #set(String, Object, ExpiringMap.ExpirationPolicy, long, TimeUnit, Consumer[])}
     * but the value stored inside will be refreshed after every access that means it's expiration time
     * will be set to {@code duration} again
     *
     * @param key              The key
     * @param value            The value
     * @param duration         The duration
     * @param unit             The time unit
     * @param removalListeners The listeners if the value is removed
     */
    public void setExpireAfterAccess(String key, Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(key, value, ExpiringMap.ExpirationPolicy.ACCESSED, duration, unit, removalListeners);
    }

    public void setExpireAfterAccess(Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(value, ExpiringMap.ExpirationPolicy.ACCESSED, duration, unit, removalListeners);
    }

    /**
     * Similar function as {@link #set(String, Object, ExpiringMap.ExpirationPolicy, long, TimeUnit, Consumer[])}
     * but the value stored inside will be removed after {@code duration} after setting the value
     *
     * @param key              The key
     * @param value            The value
     * @param duration         The duration
     * @param unit             The time unit
     * @param removalListeners The listeners if the value is removed
     */
    public void setExpireAfterCreation(String key, Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(key, value, ExpiringMap.ExpirationPolicy.CREATED, duration, unit, removalListeners);
    }

    public void setExpireAfterCreation(Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(value, ExpiringMap.ExpirationPolicy.CREATED, duration, unit, removalListeners);
    }

    /**
     * Gets a value stored before by key
     *
     * @param key      The key
     * @param supplier The supplier if the value does not exist
     * @return The value
     */
    public <V> V get(String key, Supplier<V> supplier) {
        Object o = CONTEXT_CACHE.get(getSendersUniqueId(), getCommand().getLabel() + ":" + key);
        if (o == null && supplier != null) {
            o = supplier.get();
            this.set(o);
        }
        return (V) o;
    }

    public <V> V get(String key) {
        return get(key, null);
    }

    /**
     * Gets a value stored before by class name
     *
     * @param c        The class
     * @param supplier The supplier if the value does not exist
     * @return The value
     */
    public <V> V get(Class<?> c, Supplier<V> supplier) {
        return get(c.getName(), supplier);
    }

    public <V> V get(Class<?> c) {
        return get(c, null);
    }

}
