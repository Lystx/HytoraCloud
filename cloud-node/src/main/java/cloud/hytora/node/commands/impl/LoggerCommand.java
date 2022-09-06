package cloud.hytora.node.commands.impl;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.ArgumentHelp;
import cloud.hytora.driver.commands.help.ArgumentHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.config.MainConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggerCommand {

    @ArgumentHelp
    public void argumentHelp(ArgumentHelper helper) {
        helper.react(0, () -> {
            helper.setHelpMessages(
                    "§cUse §elogger <LogLevel>!",
                    "§cAvailable types: " + Arrays.stream(LogLevel.values()).map(LogLevel::name).collect(Collectors.toList()).toString().replace("[", "").replace("]", "")
            );
        });
    }

    @TabCompletion
    public void tabComplete(TabCompleter completer) {
        completer.react(1, Arrays.stream(LogLevel.values()).map(LogLevel::name).collect(Collectors.toList()));
    }


    @Command(
            label = "logger",
            usage = "[level]",
            flags = "?[]",
            permission = "cloud.command.logger",
            scope = CommandScope.CONSOLE
    )
    public void execute(CommandContext<?> context, CommandArguments args) {


        if (args.size() != 1) {
            context.sendHelp();
            return;
        }
        LogLevel level = LogLevel.fromName(args.getString(0, "INFO"));

        try {
            MainConfiguration instance = MainConfiguration.getInstance();
            instance.setLogLevel(level);

            NodeDriver.getInstance().getConfigManager().setConfig(instance);
            NodeDriver.getInstance().getConfigManager().save();


            CloudDriver.getInstance().getLogger().setMinLevel(level);
            context.sendMessage("Changed LogLevel to {}", level);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
