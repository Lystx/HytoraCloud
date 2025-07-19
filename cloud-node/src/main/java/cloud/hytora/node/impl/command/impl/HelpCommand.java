package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.command.sender.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(
        value = {"help", "?"},
        permission = "cloud.hytora.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Shows this men"

)

public class HelpCommand {

    @Command.Root
    public void onHelpCommand(CommandSender sender) {
        sender.sendMessage("§8");
        sender.sendMessage("§7Commands§8:");

        List<String> duplicates = new ArrayList<>();

        for (RegisteredCommand command : CloudDriver.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(RegisteredCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getScope().covers(sender)) {
                continue;
            }
            if (command.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {
                continue;
            }
            if (duplicates.stream().anyMatch(s -> Arrays.asList(command.getNames()).contains(s))) {
                continue;
            }
            duplicates.addAll(Arrays.asList(command.getNames()));

            List<String> aliases = new ArrayList<>(Arrays.asList(command.getNames()));
            aliases.remove(0); //removing main command trigger
            sender.sendMessage("  §8» %1" + command.getNames()[0] + "§8(%2"+ String.join("§7, " + "%2", (aliases.isEmpty() ? "§c/" : aliases.toString()).replace("[", "").replace("]", "") + "§8) × §f" + (!command.getMainDescription().trim().isEmpty() ? command.getMainDescription() : "No Description")));
        }
        sender.sendMessage("§8");
    }
}
