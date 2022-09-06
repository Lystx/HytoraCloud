package cloud.hytora.node.console.jline2.helper;


import cloud.hytora.common.logging.formatter.SpacePadder;
import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jline.console.ConsoleReader;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

    @Command(label = "help", aliases = {"?"}, usage = "[page]")
    public void onHelp(CommandContext<?> context, CommandArguments args) {

        Integer page = args.getInt(0, 1);


        Collection<String> duplicates = new ArrayList<>();
        List<DriverCommand> commands = new ArrayList<>();

        for (DriverCommand command : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).getRootCommands().stream().sorted(Comparator.comparing(DriverCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getCommandScope().covers(context.getCommandSender())) {
                continue;
            }
            if (duplicates.stream().anyMatch(s -> command.getNames().contains(s))) { //to avoid aliases create conflicts
                continue;
            }
            duplicates.addAll(command.getNames());
            commands.add(command);

        }

        //split command help after 5 command entries
        List<List<DriverCommand>> splitCommands = CollectionUtils.splitCollection(commands, 5);
        try {
            List<DriverCommand> pagedCommands = splitCommands.get((page - 1));

            context.sendMessage("§8");
            context.sendMessage("§6=> CommandHelp Page §8[§b{}§8/§b{}§8]§8:", page, splitCommands.size());
            context.sendMessage("§8");
            for (DriverCommand command : pagedCommands) {

                StringBuilder builder = new StringBuilder();

                int triggerLength = 22;
                int permissionLength = 25;
                int descriptionLength = 25;

                String triggers = command.getNames().toString();
                String permission = (command.getPermission() == null || command.getPermission().trim().isEmpty()) ? "None" : command.getPermission();
                String description = (!command.getDescription().trim().isEmpty() ? command.getDescription() : "No Desc");


                if (triggers.length() > triggerLength) triggers = triggers.substring(triggers.length() - triggerLength);
                if (permission.length() > permissionLength) permission = permission.substring(permission.length() - permissionLength);
                if (description.length() > descriptionLength) description = description.substring(description.length() - descriptionLength);

                //command triggers
                builder.append("  §8» §7Trigger§8: §b");
                SpacePadder.padRight(builder, triggers, triggerLength);
                builder.append(" ");

                //command permission
                builder.append("§8| §7Perm§8: §b");
                SpacePadder.padRight(builder, permission, permissionLength);
                builder.append(" ");

                //command description
                builder.append("§8| §f");
                SpacePadder.padRight(builder, description, descriptionLength);

                context.sendMessage(builder.toString());
            }

            context.sendMessage("§8");
            if (page < splitCommands.size()) {
                context.sendMessage("§a=> §7Next Page§8: §ehelp {}", (page + 1));
                context.sendMessage("§8");
            } else {
                context.sendMessage("§c=> §7Previous Page§8: §ehelp {}", (page - 1));
                context.sendMessage("§8");
            }
        } catch (Exception e) {
            context.sendMessage("§cThere is no page with index §e" + page + "§c!");
        }

    }

}
