package cloud.hytora.driver.command;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.context.ApplicationContext;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.command.annotation.data.RegisteredCommandArgument;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;

import com.google.gson.internal.Primitives;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public abstract class DefaultCommandManager implements CommandManager {

    private final Map<Class<? extends CommandCompleter>, CommandCompleter> completer = new HashMap<>();
    private final Collection<RegisteredCommand> commands = new CopyOnWriteArrayList<>();
    private BiConsumer<CommandSender, String> handler = null;

    @Getter
    private final Map<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();

    @Getter
    @Setter
    private boolean active;


    public DefaultCommandManager() {
        this.active = true;
        this.registerParser(int.class, Integer::parseInt);
        this.registerParser(double.class, Double::parseDouble);
        this.registerParser(long.class, Long::parseLong);
        this.registerParser(byte.class, Byte::parseByte);
        this.registerParser(short.class, Short::parseShort);
        this.registerParser(float.class, Float::parseFloat);
        this.registerParser(boolean.class, Boolean::valueOf);
        this.registerParser(TimeUnit.class, TimeUnit::valueOf);
    }

    @Override
    public void setInActiveHandler(BiConsumer<CommandSender, String> handler) {
        this.handler = handler;
    }

    @Override
    public void registerCommand(Class<?> commandClass) {
        this.registerCommand(ReflectionUtils.createEmpty(commandClass));
    }

    @Override
    public void registerCommand(@Nonnull Object command) {
        Command commandAnnotation = command.getClass().getAnnotation(Command.class);
        CommandDescription commandDescriptionAnnotation = command.getClass().getAnnotation(CommandDescription.class);
        CommandExecutionScope executionScopeAnnotation = command.getClass().getAnnotation(CommandExecutionScope.class);
        CommandPermission commandPermissionAnnotation = command.getClass().getAnnotation(CommandPermission.class);
        CommandAutoHelp autoHelp = command.getClass().getAnnotation(CommandAutoHelp.class);

        if (autoHelp != null) {
            RegisteredCommand registeredCommand = new RegisteredCommand(
                    commandAnnotation.value(),
                    "",
                    "",
                    new String[]{"", "help", "?", " "},
                    commandPermissionAnnotation.value(),
                    "Shows this help page",
                    commandDescriptionAnnotation != null ? commandDescriptionAnnotation.value() : "",
                    executionScopeAnnotation.value(),
                    new ArrayList<>(),
                    null,
                    command
            );
            commands.add(registeredCommand);
        }

        for (Method method : ReflectionUtils.getMethodsAnnotatedWith(command.getClass(), Command.class, Root.class)) {
            Command pathAnnotation = method.getAnnotation(Command.class);
            Syntax syntaxAnnotation = method.getAnnotation(Syntax.class);
            Root rootAnnotation = method.getAnnotation(Root.class);
            CommandPermission permission = method.getAnnotation(CommandPermission.class);

            List<RegisteredCommandArgument> arguments = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                if (!parameter.isAnnotationPresent(Argument.class)) {
                    continue;
                }
                Argument argumentAnnotation = parameter.getAnnotation(Argument.class);
                arguments.add(new RegisteredCommandArgument(argumentAnnotation.value(), parameter.getType(), argumentAnnotation.completer(), argumentAnnotation.words(), argumentAnnotation.raw(), argumentAnnotation.optional()));
            }
            if (rootAnnotation != null) {

                RegisteredCommand cmd = new RegisteredCommand(
                        commandAnnotation.value(),
                        "",
                        "",
                        new String[0],
                        permission != null ? permission.value() : (commandPermissionAnnotation != null ? commandPermissionAnnotation.value() : ""),
                        method.getAnnotation(CommandDescription.class) != null ? method.getAnnotation(CommandDescription.class).value() : "",
                        commandDescriptionAnnotation != null ? commandDescriptionAnnotation.value() : "",
                        method.getAnnotation(CommandExecutionScope.class) == null ? command.getClass().getAnnotation(CommandExecutionScope.class).value() : method.getAnnotation(CommandExecutionScope.class).value(),
                        new ArrayList<>(),
                        method,
                        command

                );

                commands.add(cmd);
                continue;
            }
            for (String path : (pathAnnotation == null ? new String[]{""} : pathAnnotation.value())) {
                RegisteredCommand registeredCommand = new RegisteredCommand(
                        commandAnnotation.value(),
                        path,
                        syntaxAnnotation == null ? "" : syntaxAnnotation.value(),
                        pathAnnotation == null ? new String[0] : pathAnnotation.value(),
                        commandPermissionAnnotation == null ? "" : commandPermissionAnnotation.value(),
                        method.getAnnotation(CommandDescription.class) != null ? method.getAnnotation(CommandDescription.class).value() : "",
                        commandDescriptionAnnotation != null ? commandDescriptionAnnotation.value() : "",
                        method.getAnnotation(CommandExecutionScope.class) == null ? command.getClass().getAnnotation(CommandExecutionScope.class).value() : method.getAnnotation(CommandExecutionScope.class).value(),
                        arguments,
                        method,
                        command
                );
                commands.add(registeredCommand);

            }

        }

        handleCommandChange();
    }

    @Override
    public <T> void registerParser(Class<T> typeClass, ArgumentParser<T> parser) {
        if (typeClass.isPrimitive()) {
            Class<T> wrapperClass = Primitives.wrap(typeClass);
            this.parsers.put(wrapperClass, parser);
        }
        this.parsers.put(typeClass, parser);
    }

    @Override
    public void unregisterCommand(@Nonnull Object instance) {
        commands.removeIf(command -> command.getInstance() == instance);
        handleCommandChange();
    }

    @Override
    public void unregisterCommand(@Nonnull ClassLoader classLoader) {
        commands.removeIf(command -> command.getInstance().getClass().getClassLoader() == classLoader);
        handleCommandChange();
    }

    @Override
    public void unregisterCommand(@Nonnull String name) {
        commands.removeIf(command -> Arrays.asList(command.getNames()).contains(name));
        handleCommandChange();
    }

    protected abstract void handleCommandChange();

    public void updateIngameCommands() {
        Set<CommandObject> ingameCommands = new HashSet<>();
        for (RegisteredCommand command : commands) {
            if (command.getScope().isIngame()) {
                for (String name : command.getNames()) {
                    ingameCommands.add(new CommandObject((command.getScope() == cloud.hytora.driver.command.CommandScope.CONSOLE_AND_INGAME ? "cloud " : "") + name, command.getPermission(), command.getScope()));
                }
            }
        }

        CloudDriver.getInstance().getStorage().set("ingameCommands", ingameCommands).update();
    }

    @Nonnull
    @Override
    public Collection<RegisteredCommand> getCommands() {
        return Collections.unmodifiableCollection(commands);
    }

    @Nonnull
    @Override
    public Collection<String> completeCommand(@Nonnull CommandSender sender, @Nonnull String input) {
        String lowered = input.toLowerCase();

        Collection<String> suggestions = new CopyOnWriteArraySet<>();
        command:
        for (RegisteredCommand command : commands) {
            if (!command.getScope().covers(sender)) continue;
            if (!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) continue;

            // if the command begins with the cloud prefix, remove it
            String stripped = lowered;
            if (command.getScope().isUniversal() && sender instanceof PlayerCommandSender) {
                if (lowered.startsWith("cloud"))
                    stripped = lowered.substring("cloud".length());
                while (stripped.startsWith(" "))
                    stripped = stripped.substring(1);
            }

            for (String name : command.getNames()) {
                boolean startsWithName = stripped.startsWith(name);
                boolean startsWithInput = name.startsWith(stripped);
                if (!startsWithName && !startsWithInput) {
                    continue;
                }
                if (!startsWithName || name.equalsIgnoreCase(stripped)) {
                    suggestions.add(name);
                    continue;
                }

                // the current alt name of the command was used
                String remaining = stripped.substring(name.length());
                // check whether this name was really used: input: player, name: players -> incorrect
                if (!remaining.startsWith(" ") && !remaining.isEmpty()) continue;

                while (remaining.startsWith(" "))
                    remaining = remaining.substring(1);

                String[] args = remaining.split(" ");
                if (remaining.endsWith(" ")) {
                    // we want to get the next argument
                    args = Arrays.copyOf(args, args.length + 1);
                    args[args.length - 1] = "";
                }

                String path = command.getPath();
                String[] pathArgs = path.split(" ");
                for (int p = 0, a = 0; p < pathArgs.length; p++, a++) {
                    // p: the current path args index
                    // a: the current given args index
                    // d: the current dynamic argument index

                    String currentPathArg = pathArgs[p];
                    String currentGivenArg = args[a];
                    boolean dynamicArg = currentPathArg.startsWith("<") && currentPathArg.endsWith(">");
                    if (dynamicArg) {
                        RegisteredCommandArgument argument = command.getArgument(currentPathArg.replace("<", "").replace(">", ""));
                        if (argument.getWords() > 1)
                            a += argument.getWords() - 1;
                    }

                    if (p + 1 == args.length) {
                        if (dynamicArg) {
                            RegisteredCommandArgument argument = command.getArgument(currentPathArg.replace("<", "").replace(">", ""));

                            if (argument.getWords() != 1) continue command;

                            CommandCompleter completer = getCompleter(argument.getCompleterClass());
                            Collection<String> supplied = completer.complete(sender, input, currentGivenArg);

                            if (supplied.isEmpty()) {
                            } else if (argument.isRaw()) {
                                suggestions.addAll(supplied);
                            } else {
                                List<String> list = supplied instanceof ArrayList ? (List<String>) supplied : new ArrayList<>(supplied);

                                list.removeIf(current -> !current.toLowerCase().startsWith(currentGivenArg));
                                Collections.sort(list);

                                suggestions.addAll(list);
                            }

                        } else {
                            if (!currentPathArg.isEmpty())
                                suggestions.add(currentPathArg);
                        }

                        continue command; // with other alt names we will get the same output, so we can skip them
                    }
                    if (!currentGivenArg.equalsIgnoreCase(currentPathArg))
                        continue command; // incorrect path, skip command
                }

            }

        }

        List<String> list = new ArrayList<>(suggestions);
        Collections.sort(list);
        return list;
    }

    @Override
    public void executeCommand(@Nonnull CommandSender sender, @Nonnull String input) {

        if (!this.isActive()) {
            if (this.handler != null) {
                handler.accept(sender, input);
            }
            return;
        }

        command:
        for (RegisteredCommand command : commands) {
            if (!command.getScope().covers(sender)) {
                continue;
            }

            // if the command begins with the cloud prefix, remove it
            String stripped = input;
            if (command.getScope().isUniversal() && sender instanceof PlayerCommandSender) {
                if (input.toLowerCase().startsWith("cloud"))
                    stripped = input.toLowerCase().substring("cloud".length());
                while (stripped.startsWith(" "))
                    stripped = stripped.substring(1);
            }

            for (String name : command.getNames()) {
                boolean startsWithName = stripped.startsWith(name);
                boolean startsWithInput = name.startsWith(stripped);
                if (!startsWithName && !startsWithInput) {
                    continue;
                }
                if (!startsWithName) {
                    continue;
                }

                // the current alt name of the command was used
                String remaining = stripped.substring(name.length());
                // check whether this name was really used: input: player, name: players -> incorrect
                if (!remaining.startsWith(" ") && !remaining.isEmpty()) continue;

                while (remaining.startsWith(" "))
                    remaining = remaining.substring(1);

                String[] args = remaining.split(" ");

                String path = command.getPath();
                Map<String, Object> argumentValues = new LinkedHashMap<>();
                String[] pathArgs = path.split(" ");
                for (int p = 0, a = 0; p < pathArgs.length; p++, a++) {
                    // p: the current path args index
                    // a: the current given args index

                    String currentPathArg = pathArgs[p];
                    boolean dynamicArg = currentPathArg.startsWith("<") && currentPathArg.endsWith(">");
                    if (dynamicArg) {
                        RegisteredCommandArgument argument = command.getArgument(currentPathArg.replace("<", "").replace(">", ""));
                        if (args.length <= a + argument.getWords()) {
                            if (argument.isOptional()) continue command;
                        }
                        String[] valueArray = Arrays.copyOfRange(args, a, a + argument.getWords());
                        String value = StringUtils.getArrayAsString(valueArray, " ");

                        if (argument.getObjectClass() != String.class) {
                            ArgumentParser<?> argumentParser = this.parsers.get(argument.getObjectClass());
                            if (argumentParser != null) {
                                try {
                                    Object parse = argumentParser.parse(value);
                                    argumentValues.put(argument.getName(), parse);
                                } catch (Exception e) {
                                    CloudDriver.getInstance().getLogger().warn("Couldn't parse '" + value + "' to Object of type " + argument.getObjectClass() + "!");
                                    return;
                                }
                            } else {
                                argumentValues.put(argument.getName(), value);
                            }
                        } else {
                            argumentValues.put(argument.getName(), value);
                        }

                        if (argument.getWords() > 1)
                            a += argument.getWords() - 1;

                        if (args.length <= a) continue command;
                    } else {
                        if (args.length <= a) continue command;
                        if (!currentPathArg.equalsIgnoreCase(args[a]))  // incorrect path, skip command
                            continue command;
                    }

                    if (p + 1 >= pathArgs.length) {
                        execute(command, sender, argumentValues);
                        return;
                    }

                }

            }

        }

        sender.sendMessage("§cType 'help' to see all executable commands!");
    }

    protected void execute(@Nonnull RegisteredCommand command, @Nonnull CommandSender sender, @Nonnull Map<String, Object> argumentValues) {
        if (!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
            sender.sendMessage("§cYou do not match the required permission for this command!");
            return;
        }

        Parameter[] methodsParameters = command.getMethod() == null ? new Parameter[0] : command.getMethod().getParameters();
        Object[] parameters = new Object[methodsParameters.length];
        for (int i = 0; i < methodsParameters.length; i++) {
            Parameter parameter = methodsParameters[i];
            if (CommandSender.class.isAssignableFrom(parameter.getType())) {
                parameters[i] = sender;
            } else if (parameter.isAnnotationPresent(Argument.class)) {
                parameters[i] = argumentValues.get(parameter.getAnnotation(Argument.class).value());
            }
        }

        try {
            if (command.getMethod() == null) {
                //indicate auto help

                Command annotation = command.getInstance().getClass().getAnnotation(Command.class);

                sender.sendMessage("§8");
                sender.sendMessage("Help for Command '" + annotation.value()[0] + "':");
                List<RegisteredCommand> registeredCommands = CloudDriver.getInstance()
                        .getCommandManager().getCommands()
                        .stream()
                        .filter(
                                c -> Arrays.stream(c.getNames())
                                        .anyMatch(
                                                s -> Arrays.asList(annotation.value())
                                                        .contains(s)
                                        )
                        ).collect(Collectors.toList());


                registeredCommands.sort(new Comparator<RegisteredCommand>() {
                    @Override
                    public int compare(RegisteredCommand o1, RegisteredCommand o2) {
                        // -1 = less , 0 = equal , 1 = higher

                        return o1.getPath().compareTo(o2.getPath());
                    }
                });
                for (RegisteredCommand registeredCommand : registeredCommands) {
                    if (registeredCommand.getPath().trim().isEmpty() || !registeredCommand.getScope().covers(sender)) {
                        continue;
                    }
                    sender.sendMessage("§b" + registeredCommand.getNames()[0] + " " + registeredCommand.getPath() + " | §7" + (registeredCommand.getDescription().trim().isEmpty() ? registeredCommand.getMainDescription() : registeredCommand.getDescription()));
                }
                sender.sendMessage("§8");
                return;
            }
            command.getMethod().invoke(command.getInstance(), parameters);
        } catch (Throwable ex) {
            sender.sendMessage("§cAn error occurred whilst attempting to perform this command!");
            CloudDriver.getInstance().getLogger().error("An error occurred while handling command '" + command.getNames()[0] + "'");
            ex.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public CommandCompleter getCompleter(@Nonnull Class<? extends CommandCompleter> completerClass) {
        return completer.computeIfAbsent(completerClass, key -> {
            try {
                return completerClass.newInstance();
            } catch (Throwable ex) {
                throw new IllegalStateException("Could not create command completer instance of " + completerClass.getName(), ex);
            }
        });
    }
}
