package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
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
        permission = "cloud.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Shows this men"

)
@ApplicationParticipant
public class HelpCommand {

    @Command.Root
    public void onHelpCommand(CommandSender sender) {
        sender.sendMessage("§8");
        sender.sendMessage("§7Commands§8:");

        java.util.List<String> duplicates = new ArrayList<String>();

        for (RegisteredCommand command : CloudDriver.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(RegisteredCommand::getPath)).collect(Collectors.toList())) {
            if (!command.getScope().covers(sender)) {
                continue;
            }
            if (duplicates.stream().anyMatch(s -> Arrays.asList(command.getNames()).contains(s))) {
                continue;
            }
            duplicates.addAll(Arrays.asList(command.getNames()));

            List<String> aliases = new ArrayList<>(Arrays.asList(command.getNames()));
            aliases.remove(0); //removing main command trigger
            sender.sendMessage("§b" + command.getNames()[0] + "§8(§b"+ String.join("§7, " + "§b", (aliases.isEmpty() ? "§c/" : aliases.toString()).replace("[", "").replace("]", "") + "§8) × §f" + (command.getDescription().trim().isEmpty() ? command.getMainDescription() : "No Description")));
        }
        sender.sendMessage("§8");
    }
}
