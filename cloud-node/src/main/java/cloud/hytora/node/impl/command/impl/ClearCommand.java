package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.context.annotations.CacheContext;
import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandExecutionScope;
import cloud.hytora.driver.command.annotation.Root;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.node.NodeDriver;

@Command("clear")
@CommandExecutionScope(CommandScope.CONSOLE)
@CommandDescription("Clears the console")
@ApplicationParticipant
public class ClearCommand {

    @CacheContext
    private NodeDriver nodeDriver;

    @Constructor
    public void init() {
        nodeDriver.logToExecutor(NetworkComponent.of("Node-1"), "Hello my friend I jus gud registed!");
    }

    @Root
    public void executeClear(CommandSender sender) {
        NodeDriver.getInstance().getConsole().clearScreen();

    }
}
