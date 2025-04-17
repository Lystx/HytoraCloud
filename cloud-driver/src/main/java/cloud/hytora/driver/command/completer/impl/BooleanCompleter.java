package cloud.hytora.driver.command.completer.impl;

import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class BooleanCompleter implements CommandCompleter {

    @NotNull
    @Override
    public Collection<String> complete(@NotNull CommandSender sender, @NotNull String argument) {
        return Arrays.asList("true", "false");
    }
}
