package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class ConfigurationCompleter implements CommandCompleter {

    @NotNull
    @Override
    public Collection<String> complete(@NotNull CommandSender sender, @NotNull String message, @NotNull String argument) {
        return CloudDriver.getInstance().getConfigurationManager().getAllCachedConfigurations().stream().map(ServerConfiguration::getName).collect(Collectors.toList());
    }
}
