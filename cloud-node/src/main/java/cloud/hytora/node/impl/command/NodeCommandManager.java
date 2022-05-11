package cloud.hytora.node.impl.command;

import cloud.hytora.driver.command.DefaultCommandManager;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

public class NodeCommandManager extends DefaultCommandManager {

    public NodeCommandManager() {
        this.setActive(true);
    }

    @Override
    protected void handleCommandChange() {
        updateIngameCommands();
    }

    @Override
    public @NotNull ConsoleCommandSender getThisSidesCommandSender() {
        return (ConsoleCommandSender) NodeDriver.getInstance().getCommandSender();
    }
}
