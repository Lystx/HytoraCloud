package cloud.hytora.driver.commands.data;

import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.data.enums.CommandType;
import cloud.hytora.driver.commands.events.CommandErrorEvent;
import cloud.hytora.driver.commands.events.CommandHelpEvent;
import cloud.hytora.driver.commands.events.PreCommandEvent;
import cloud.hytora.driver.commands.help.CommandHelp;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.event.IEventManager;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * A wrapper class of {@link Command} and the class which executes the command itself
 */
@Getter @Setter
public class DriverCommand {

    /**
     * The help flag
     */
    public static final String HELP_FLAG = "?";

    /**
     * The help flag
     */
    public static final String DESC_FLAG = "desc";

    /**
     * All predefined flags
     */
    public static final String[] PREDEFINED_FLAGS = {HELP_FLAG, DESC_FLAG};

    /**
     * The path indicator/separator
     */
    public static final String PATH = ".";

    private String label;
    private String parentName;
    private List<String> aliases;
    private String description;
    private String permission;
    private String[] autoHelpAliases;
    private List<String> flags;
    private CommandUsage usage;
    private CommandScope commandScope;
    private boolean invalidUsageIfEmptyInput;

    /**
     * The label of the command (e.g.: command.subcommand.subsubcommand)
     */
    private String path;

    /**
     * Type of the command (e.g. Root or Sub)
     */
    private CommandType commandType;

    /**
     * If the command is not the root command than would be this value != null
     */
    @Setter
    private DriverCommand root;

    /**
     * If the command is a subCommand this parent is the parent command (otherwise null)
     */
    @Setter
    private DriverCommand parent;

    /**
     * Children of this command (if available)
     */
    private Map<String, DriverCommand> children = new HashMap<>();

    /**
     * The class object of the class where the command method is initialised
     */
    private Object methodClassObject;

    /**
     * The method itself
     *
     * @see #invokeMethod(CommandContext, CommandArguments)
     */
    private final Method method;

    /**
     * Tab completor objects. Method is the tabCompletion method and the object the declared
     * class object
     *
     * @see TabCompletion
     */
    private Map<Method, Object> tabCompletionMap = new HashMap<>();

    /**
     * Argument helper methods. These methods can be executed to send information about one argument and its usage
     *
     * @see CommandHelp
     */
    private Map<Method, Object> argumentHelperMap = new HashMap<>();

    public DriverCommand(Object methodClassObject, @Nullable Method method, Command annotation) {
        this.methodClassObject = methodClassObject;
        this.method = method;
        if(method != null && !method.isAnnotationPresent(Command.class)) {
            return;
        }

        this.label = annotation.label();
        this.parentName = annotation.parent();
        this.aliases = StringUtils.modifyStringList(Arrays.asList(annotation.aliases()), String::toLowerCase);
        this.description = annotation.desc();
        this.permission = annotation.permission();
        this.flags = Arrays.asList(annotation.flags());
        this.usage = new CommandUsage(annotation.usage());
        this.invalidUsageIfEmptyInput = annotation.invalidUsageIfEmptyInput();
        this.autoHelpAliases = annotation.autoHelpAliases();
        this.commandScope = annotation.scope();

        // remove empties
        this.aliases = StringUtils.removeEmpties(aliases);
        this.flags = StringUtils.removeEmpties(flags);

        this.commandType = (parentName == null || parentName.trim().isEmpty()) ? CommandType.ROOT : CommandType.SUB;
    }

    /**
     * Initialises the {@link #path}
     */
    public void initTreePath() {
        if(this.path != null) return;
        StringBuilder path = new StringBuilder(label);

        DriverCommand current = this;
        DriverCommand parent;
        while((parent = current.getParent()) != null){
            current = parent;
            path.insert(0, parent.getLabel() + PATH);
        }
        this.path = path.toString();
    }

    public Collection<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add(label);
        names.addAll(getAliases());
        return names;
    }

    /**
     * Executes the tabcompletion for given tab completor
     *
     * @param completer The tab completor
     */
    public void executeTabCompletion(TabCompleter completer) {
        DriverCommand root = getRoot();
        Map<Method, Object> map = root == null ? tabCompletionMap : root.getTabCompletionMap();

        for(Method m : map.keySet()) {
            ReflectionUtils.invokeMethod(m, map.get(m), completer);
        }
    }

    /**
     * Executes the argument helper for given helper
     *
     * @param helper The argument helper
     */
    public void executeArgumentHelper(CommandHelper helper) {
        DriverCommand root = getRoot();
        Map<Method, Object> map = root == null ? argumentHelperMap : root.getArgumentHelperMap();

        for(Method m : map.keySet()) {
            ReflectionUtils.invokeMethod(m, map.get(m), helper);
        }
    }

    /**
     * Adds a tabcompletion to the map of tabCompletions
     *
     * @param tabCompletionMethod The tabCompletion method
     */
    public void addTabCompletion(Object instance, Method tabCompletionMethod) {
        if(tabCompletionMethod == null) return;
        this.tabCompletionMap.put(tabCompletionMethod, instance);
    }

    /**
     * Adds an argument helper method to the list
     *
     * @param method The method
     */
    public void addArgumentHelper(Object instance, Method method) {
        this.argumentHelperMap.put(method, instance);
    }

    /**
     * Gets a list of the path
     *
     * @return The list
     */
    public List<String> getWholePath() {
        return Arrays.asList(path.split("\\."));
    }

    /**
     * Gets the path before this command (all parents)
     *
     * @return The list
     */
    public List<String> getBeforePath() {
        List<String> whole = getWholePath();
        return whole.size() == 1 ? new ArrayList<>() : whole.subList(0, whole.size() - 1);
    }

    /**
     * Checks if the command has a parent
     *
     * @return The result
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Checks if this command has childrens
     *
     * @return The result
     */
    public boolean hasChildren() {
        return getChildrens().size() != 0;
    }

    /**
     * Adds a children to the children list
     *
     * @param command The command
     */
    public void addChildren(DriverCommand command) {
        this.children.put(command.getLabel(), command);
    }

    /**
     * Get the list of children
     *
     * @return The list
     */
    public List<DriverCommand> getChildrens() {
        return new ArrayList<>(children.values());
    }

    public DriverCommand getChildren(String label) {
        for(DriverCommand ci : getChildrens()) {
            if(ci.getLabel().equalsIgnoreCase(label)
                    || ci.getAliases().contains(label.toLowerCase())) return ci;
        }
        return null;
    }

    /**
     * Get the children map
     *
     * @return The map
     */
    public Map<String, DriverCommand> getChildrenMap() {
        return children;
    }

    public Tuple<DriverCommand, String[]> getInstance(String[] args, Function<DriverCommand, Boolean> preCommand) {
        if(preCommand != null && !preCommand.apply(this)) {
            return null;
        }

        // check for sub command
        if(args.length != 0 && getChildrenMap().containsKey(args[0])) {
            DriverCommand children = getChildrenMap().get(args[0]);

            return children.getInstance(Arrays.copyOfRange(args, 1, args.length), preCommand);
        }
        return new Tuple<>(this, args);
    }

    /**
     * Executes this command
     *
     * @param context The command context
     * @param strings    The arguments
     * @param <T>     The type
     * @return The result
     */
    public <T extends CommandSender> boolean execute(CommandContext<T> context, String[] strings) {
        // list command instance atg last argument (subcommands)
        PreCommandEvent[] event = new PreCommandEvent[1];
        Tuple<DriverCommand, String[]> command = getInstance(strings, commandInstance -> {
            event[0] = new PreCommandEvent<>(context, commandInstance);
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(event[0]);
            return !event[0].isCancelled();
        });
        if(command == null) {
            return false;
        }
        DriverCommand instance = command.getFirst();
        context.setCommand(instance);

        // set parameter set
        CommandArguments args = new CommandArguments(instance, command.getSecond());
        context.setParamSet(args);

        if (args.isEmpty() && invalidUsageIfEmptyInput && strings.length <= 0) {
            context.sendHelp();
        } else if (strings.length >= 1 && Arrays.stream(this.autoHelpAliases).anyMatch(s -> s.equalsIgnoreCase(strings[0]))) {
            context.sendHelp();
        }

        // check for command help
        if(args.hasFlag(HELP_FLAG)) {
            if(!getFlags().contains(HELP_FLAG)) {
                // the user wants to auto handle the help flag
                CommandHelpEvent helpEvent = new CommandHelpEvent(context, false);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(helpEvent);

                if(helpEvent.isCancelled()) {
                    return true;
                }
            }
            // the user wants to custom handle the help flag
        }

        // check for command help
        if(args.hasFlag(DESC_FLAG)) {
            if(!getFlags().contains(DESC_FLAG)) {
                // the user wants to auto handle the desc flag
                CommandHelpEvent helpEvent = new CommandHelpEvent(context, true);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(helpEvent);

                if (helpEvent.isCancelled()) {
                    return true;
                }
            }
            // the user wants to custom handle the help flag
        }

        // check length of arguments
        if(args.size() < context.getCommand().getUsage().getNeededSize()) {
            if (args.isEmpty() && context.getCommand().isInvalidUsageIfEmptyInput()) {
                //if input empty & triggers invalid use then nothing needs to happen
            } else {
                context.invalidUsage();
                return false;
            }
        }

        // execute command
        if(event[0].getService() != null) {
            event[0].getService().execute(() -> invokeMethod(context, args));
        }
        else {
            return invokeMethod(context, args);
        }
        return true;
    }

    /**
     * Executes the method
     *
     * @param context  The context
     * @param paramSet The parameterSet
     * @param <T>      The type
     * @return The result
     */
    private <T extends CommandSender> boolean invokeMethod(CommandContext<T> context, CommandArguments paramSet) {
        try {
            DriverCommand command = context.getCommand();
            if (command.getMethod() != null) {
                command.getMethod().invoke(command.getMethodClassObject(), context, paramSet);
            }
        } catch(Exception e) {
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(new CommandErrorEvent<>(context, this, e));
            return false;
        }
        return true;
    }

}
