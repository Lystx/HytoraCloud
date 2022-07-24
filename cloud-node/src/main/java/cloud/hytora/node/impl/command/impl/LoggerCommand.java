package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.tree.ArrayTypeSignature;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Command(
        name = {"logger", "log"},
        scope = CommandScope.CONSOLE
)
@CommandDescription("Changes the level of Logger")
@CommandAutoHelp
public class LoggerCommand {

    @SubCommand("setLevel <level>")
    public void executeClear(
            CommandSender sender,
            @CommandArgument(value = "level", completer = Completer.class) LogLevel level
            ) {

        try {
            MainConfiguration instance = MainConfiguration.getInstance();
            instance.setLogLevel(level);

            NodeDriver.getInstance().getConfigManager().setConfig(instance);
            NodeDriver.getInstance().getConfigManager().save();


            CloudDriver.getInstance().getLogger().setMinLevel(level);
            sender.sendMessage("Changed LogLevel to {}", level);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static class Completer implements CommandCompleter {

        @NotNull
        @Override
        public Collection<String> complete(@NotNull CommandSender sender, @NotNull String message, @NotNull String argument) {
            return Arrays.stream(LogLevel.values()).map(LogLevel::getName).collect(Collectors.toList());
        }
    }
}
