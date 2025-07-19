package cloud.hytora.bridge.minecraft.command;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.common.message.IMessageChannel;
import cloud.hytora.driver.entity.services.fallback.SimpleFallback;

import static cloud.hytora.driver.command.CommandScope.INGAME_SPIGOT;

@Command(
        value = {"reload", "rl", "minecraft:reload", "minecraft:rl"},
        description = "Reload command",
        executionScope = INGAME_SPIGOT
)
public class ReloadCommand {

    @Command.Root
    public void execute(PlayerCommandSender sender) {
       sender.sendMessage("§cThis command has been disabled by §eHytoraCloud §cto prevent §eissues§c!");
    }
}