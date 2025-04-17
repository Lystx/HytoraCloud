package cloud.hytora.driver.command.completer.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.networking.NetworkComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CloudServerCompleter implements CommandCompleter {

    @NotNull
    @Override
    public Collection<String> complete(@NotNull CommandSender sender, @NotNull String argument) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().map(NetworkComponent::getName).collect(Collectors.toList());
    }
}
