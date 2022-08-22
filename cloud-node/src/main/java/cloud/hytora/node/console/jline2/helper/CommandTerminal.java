package cloud.hytora.node.console.jline2.helper;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jline.console.ConsoleReader;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandTerminal {

    /**
     * The executor service to run something async
     */
    private final ExecutorService executorService = Executors
            .newCachedThreadPool(
                    new ThreadFactoryBuilder()
                            .setNameFormat("commanding-pool-%d")
                            .build()
            );

    /**
     * Is the command terminal already initialised?
     */
    @Getter
    private boolean initialised = false;

    /**
     * The terminal task
     */
    @Getter
    private TerminalTask terminalTask;

    /**
     * Starts the commanding system of the console<br>
     * The commanding system is an implementation of the jline console
     * and it supports a prompt and some other nice features
     *
     * @param reader The console reader for the jline implementation
     */
    public void start(ConsoleReader reader) {
        if (initialised) {
            return;
        }

        ICommandManager commandManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class);
        commandManager.registerCommands(this);
        commandManager.registerEventAdapter(new ConsoleCommandEventAdapter());

        // task for executing commands (jline)
        this.terminalTask = new TerminalTask(reader, s -> {
            ScreenManager screenManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
            Screen currentScreen = screenManager.getCurrentScreen();
            if (currentScreen == null) {
                return;
            }
            for (Consumer<? super String> inputHandler : new ArrayList<>(currentScreen.getInputHandlers())) {
                inputHandler.accept(s);
            }
        });
        executorService.execute(this.terminalTask);
        initialised = true;
    }

    /**
     * Stops the executor service and therefore the commanding thread
     */
    public void stop() {
        executorService.shutdownNow();
        initialised = false;
    }

    @Command(label = "help", aliases = {"?"})
    public void onHelp(CommandContext<?> context, CommandArguments args) {

        context.sendMessage("§8");
        context.sendMessage("§7Commands§8:");

        Collection<String> duplicates = new ArrayList<>();

        for (DriverCommand command : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).getRootCommands().stream().sorted(Comparator.comparing(DriverCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getCommandScope().covers(context.getCommandSender())) {
                continue;
            }
            if (duplicates.stream().anyMatch(s -> command.getNames().contains(s))) {
                continue;
            }
            duplicates.addAll(command.getNames());

            List<String> aliases = (List<String>) command.getNames();
            aliases.remove(0); //removing main command trigger
            context.sendMessage("§b" + command.getNames().stream().findFirst().get() + "§8(§b" + String.join("§7, " + "§b", (aliases.isEmpty() ? "§c/" : aliases.toString()).replace("[", "").replace("]", "") + "§8) × §f" + (!command.getDescription().trim().isEmpty() ? command.getDescription() : "No Description")));
        }
        context.sendMessage("§8");
    }

}
