package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.formatter.UncoloredMessageFormatter;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.config.ConfigManager;
import cloud.hytora.driver.config.INetworkConfig;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Command(
        value = "logger",
        permission = "cloud.hytora.command.use"
)
@Command.AutoHelp

public class LoggerCommand {

    @Command(value = "setLevel", description = "Changes the level of the logger")
    @Command.Syntax("<level>")
    public void executeLevel(CommandSender sender, @Command.Argument(value = "level", completer = Completer.class) LogLevel level) {


        ConfigManager configManager = CloudDriver.getInstance().getConfigManager();
        INetworkConfig config = configManager.getConfig();

        config.setLogLevel(level);
        config.update();

        CloudDriver.getInstance().getLogger().setMinLevel(level);
        sender.sendMessage("Changed LogLevel to {}", level);

    }

    @SneakyThrows
    @Command(value = "save", description = "Saves all cached entries for given level to a file")
    @Command.Syntax("<level>")
    public void executeSave(CommandSender sender, @Command.Argument("level") LogLevel level) {

        String fileName = "log_" + System.currentTimeMillis() + ".cloud";
        Path fileToSave = new File(CloudDriver.Constants.LOG_FOLDER_EXTRA, fileName).toPath();


        FileUtils.writeToFile(fileToSave.toFile(), new Supplier<Collection<String>>() {
            @Override
            public Collection<String> get() {
                return CloudDriver
                        .getInstance()
                        .getLogger()
                        .getCachedEntries(level)
                        .stream()
                        .map(UncoloredMessageFormatter::format)
                        .collect(Collectors.toList());
            }
        });
        /*
        if (!Files.exists(fileToSave)) {
            FileUtils.createFile(fileToSave);
        }
        OutputStream stream = Files.newOutputStream(fileToSave, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        for (LogEntry cachedEntry : CloudDriver.getInstance().getLogger().getCachedEntries(level)) {
            stream.write((UncoloredMessageFormatter.format(cachedEntry) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        }
        stream.flush();
        stream.close();*/

        sender.sendMessage("§7Successfully §asaved §7logs of level §8'§e{}§8' §7to file §8'§b{}§8'!", level, fileToSave);
    }



    public static class Completer implements CommandCompleter {

        @NotNull
        @Override
        public Collection<String> complete(@NotNull CommandSender sender, @NotNull String argument) {
            return Arrays.stream(LogLevel.values()).map(LogLevel::getName).collect(Collectors.toList());
        }
    }
}
