package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.SubCommand;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.TemporaryProperties;
import cloud.hytora.node.NodeDriver;

import java.util.concurrent.TimeUnit;

@Command(
        name = {"clear", "cls"},
        scope = CommandScope.CONSOLE
)
@CommandDescription("Clears the console")
public class ClearCommand {

    @SubCommand
    public void executeClear(CommandSender sender) {
        NodeDriver.getInstance().getConsole().clearScreen();

    }
}
