package cloud.hytora.driver.commands;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.CommandUsage;
import cloud.hytora.driver.commands.data.ProtocolCommandInfo;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.data.enums.CommandType;
import cloud.hytora.driver.commands.events.CommandErrorEvent;
import cloud.hytora.driver.commands.events.CommandHelpEvent;
import cloud.hytora.driver.commands.events.CommandRegisterEvent;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.commands.parameter.DefaultParameterTypeRegistry;
import cloud.hytora.driver.commands.parameter.IParameterTypeRegistry;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.common.DriverRegistryPool;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import lombok.Getter;
import cloud.hytora.driver.commands.help.CommandHelp;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * This class registers and stores every command and the parameter definitions<br>
 * DO NOT forget to register following events somehow:<br>
 * {@link CommandErrorEvent}<br>
 * {@link TabCompleteEvent}<br>
 * {@link CommandHelpEvent}
 *
 * @see CommandParameterType
 * @see CommandEventAdapter
 */
public abstract class AbstractCommandManager extends DriverRegistryPool<String, DriverCommand> implements ICommandManager {

    @Getter
    protected final IParameterTypeRegistry paramTypeRegistry;

    @Getter
    @Setter
    protected boolean active;
    @Getter
    protected BiConsumer<CommandSender, String> inactiveHandler;

    protected AbstractCommandManager() {
        this.paramTypeRegistry = new DefaultParameterTypeRegistry();
    }


    public abstract void handleCommandChange();


    public void updateIngameCommands() {
        CloudDriver
                .getInstance()
                .getProviderRegistry()
                .get(INetworkDocumentStorage.class)
                .onTaskSucess(storage -> {

                    Set<ProtocolCommandInfo> ingameCommands = new HashSet<>();
                    for (DriverCommand command : this.getRootCommands()) {
                        if (command.getCommandScope().isIngame()) {
                            for (String name : command.getNames()) {
                                ingameCommands.add(new ProtocolCommandInfo((command.getCommandScope() == CommandScope.CONSOLE_AND_INGAME ? "cloud " : "") + name, command.getPermission(), command.getCommandScope()));
                            }
                        }
                    }
                    storage.set("ingameCommands", ingameCommands);
                });
    }

    @Override
    public void setActive(boolean active, BiConsumer<CommandSender, String> inActiveHandler) {
        this.setActive(active);
        this.inactiveHandler = inActiveHandler;
    }

    /**
     * Registers an eventAdapter which means registering its events
     *
     * @param eventAdapter The event adapter
     * @param <T>          The type
     */
    public <T extends CommandSender> void registerEventAdapter(CommandEventAdapter<T> eventAdapter) {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(eventAdapter);
    }

    /**
     * Executes a command
     *
     * @param args    The arguments
     * @param context The context
     * @param <T>     The type
     */
    public <T extends CommandSender> boolean executeCommand(String[] args, CommandContext<T> context) {
        if (!active) {
            if (inactiveHandler != null) {
                inactiveHandler.accept(context.getCommandSender(), StringUtils.getArrayAsString(args, " "));
            }
            return true;
        }
        if (args.length == 0) return false;
        String cmd = args[0];
        String[] parameter = Arrays.copyOfRange(args, 1, args.length);

        DriverCommand command = getCommand(cmd);
        return command != null && command.execute(context, parameter);
    }


    /**
     * Gets the command with given label
     *
     * @param label The label
     * @return The command
     */
    public DriverCommand getCommand(String label) {
        if (label.isEmpty()) return null;
        for (DriverCommand instance : getRegisteredCommands().values()) {
            if (instance.getNames().stream().anyMatch(s -> s.equalsIgnoreCase(label))) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Gets the most similar command from given label
     *
     * @param label The label
     * @return The command instance
     */
    public DriverCommand getSimilarCommand(String label) {
        if (label.isEmpty()) return null;
        List<String> commands = (List<String>) getAllCommandLabels();
        Map.Entry<String, Double> entry = StringUtils.getMostSimilar(label, commands, 1, true);
        if (entry == null) {
            return null;
        }
        String mostSimilar = entry.getKey();
        return getCommand(mostSimilar);
    }

    /**
     * Checks if the registry contains a command with given label
     *
     * @param label The label
     * @return The result
     */
    public boolean hasCommand(String label) {
        return getCommand(label) != null;
    }

    /**
     * Get all registered commands
     *
     * @return The map of commands
     */
    public Map<String, DriverCommand> getRegisteredCommands() {
        return keyObjectMap;
    }

    /**
     * Get the commands labels
     *
     * @return The commands labels
     */
    public List<String> getCommands() {
        return new ArrayList<>(keyObjectMap.keySet());
    }

    //all labels including alias
    public Collection<String> getAllCommandLabels() {
        Collection<String> names = new ArrayList<>();
        for (DriverCommand command : getRootCommands()) {
            names.addAll(command.getNames());
        }
        return names;
    }

    /**
     * Get the commands with the command type root
     *
     * @return The list of commands
     */
    public List<DriverCommand> getRootCommands() {
        return getRegisteredCommands().values()
                .parallelStream()
                .filter(instance -> instance.getCommandType() == CommandType.ROOT)
                .collect(Collectors.toList());
    }

    /**
     * Clears all maps
     */
    public void unregisterAll() {
        keyObjectMap.clear();
    }

    @Override
    public boolean register(String key, DriverCommand object) {
        handleCommandChange();
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(new CommandRegisterEvent(object));
        return super.register(key, object);
    }

    @Override
    public boolean unregister(String key, DriverCommand value) {
        handleCommandChange();
        return super.unregister(key, value);
    }

    @Override
    public void unregister(Class<?> cmdClass) {
        this.getRootCommands().stream().filter(cmd -> cmd.getMethodClassObject().getClass().equals(cmdClass)).findFirst().ifPresent(this::unregister);
    }

    /**
     * Register commands from given classes
     *
     * @param classes The classes
     */
    public int registerCommands(Object... classes) {
        int count = 0;
        List<DriverCommand> roots = Fetcher.fetchCommands(classes);

        for (DriverCommand root : roots) {
            if (root != null && !keyObjectMap.containsKey(root.getLabel())) {
                register(root.getLabel(), root);

                count++;
            }
        }
        return count;
    }

    public int registerCommandsSeperately(Object... classes) {
        int i = 0;
        for (Object aClass : classes) {
            i += registerCommands(aClass);
        }
        return i;
    }

    public static class Fetcher {

        /**
         * Fetches the root command and all the other commands from given classes
         *
         * @param objects The classe's objects
         * @return The root command (If no one exists return null)
         */
        private static List<DriverCommand> fetchCommands(Object... objects) {
            Map<String, DriverCommand> commands = new HashMap<>();
            Map<Method, Object> commandMethods = new HashMap<>();
            List<DriverCommand> roots = new ArrayList<>();

            // list all methods that are commandable
            for (Object o : objects) {
                Command parentCmd = o.getClass().getAnnotation(Command.class);
                if (parentCmd != null) {
                    DriverCommand parentCommand = new DriverCommand(o, null, parentCmd);

                    parentCommand.setUsage(new CommandUsage("<subcommand>"));
                    parentCommand.initTreePath();

                    roots.add(parentCommand);
                    commands.put(parentCommand.getLabel(), parentCommand);
                }
                for (Method m : o.getClass().getDeclaredMethods()) {
                    if (!checkMethod(m)) continue;
                    commandMethods.put(m, o);
                }
            }

            // list all commands
            for (Method m : commandMethods.keySet()) {
                DriverCommand instance = new DriverCommand(commandMethods.get(m), m, m.getAnnotation(Command.class));
                if (instance.getCommandType() == CommandType.ROOT) roots.add(instance);

                commands.put(instance.getLabel(), instance);
            }

            for (DriverCommand root : roots) {
                initRelations(root, commands);
            }
            Map<String, DriverCommand> newCommands = new HashMap<>();
            commands.forEach((s, commandInstance) -> newCommands.put(commandInstance.getPath(), commandInstance));

            initSpecialMethods(newCommands, objects);
            return roots;
        }

        /**
         * Initialises the relationships between the root command and the other commands
         *
         * @param root     The root command
         * @param commands The commands
         */
        private static void initRelations(DriverCommand root, Map<String, DriverCommand> commands) {
            for (DriverCommand cmd : commands.values()) {
                DriverCommand p = commands.get(cmd.getParentName());
                if (p != null) {
                    if (cmd.getParent() == null){
                        cmd.setParent(p);
                        cmd.setCommandScope(p.getCommandScope());
                    }
                    if (cmd.getRoot() == null) {
                        cmd.setRoot(root);
                    }
                    if (!p.getChildrens().contains(cmd)) {
                        p.addChildren(cmd);
                    }
                }

            }
            commands.values().forEach(DriverCommand::initTreePath);
        }

        /**
         * Initialises the tabCompletion for given commands with classes
         * So it will fetch all methods and check for occurences
         *
         * @param commands The commands
         * @param objects  The classes object's
         */
        private static void initSpecialMethods(Map<String, DriverCommand> commands, Object... objects) {
            // list all methods that are tabcompletable
            for (Object o : objects) {
                for (Method m : o.getClass().getDeclaredMethods()) {
                    for (DriverCommand cmd : commands.values()) {
                        if (cmd.getCommandType() == CommandType.ROOT) {
                            if (checkTabCompleteMethod(m))
                                cmd.addTabCompletion(o, m);
                            else if (checkArgumentHelperMethod(m))
                                cmd.addArgumentHelper(o, m);
                        }
                    }
                }
            }
        }

        /**
         * Checks if the method is commandable
         *
         * @param m The to-check method
         * @return The result
         */
        public static boolean checkMethod(Method m) {
            return ReflectionUtils.checkMethod(m, Command.class, new Class<?>[]{CommandContext.class, CommandArguments.class});
        }

        /**
         * Checks if the method is tabcompletable
         *
         * @param m The method
         * @return The result
         */
        public static boolean checkTabCompleteMethod(Method m) {
            return ReflectionUtils.checkMethod(m, TabCompletion.class, new Class<?>[]{TabCompleter.class});
        }

        /**
         * Checks if the method is argumenthelpable
         *
         * @param m The method
         * @return The result
         */
        public static boolean checkArgumentHelperMethod(Method m) {
            return ReflectionUtils.checkMethod(m, CommandHelp.class, new Class<?>[]{CommandHelper.class});
        }

    }

}
