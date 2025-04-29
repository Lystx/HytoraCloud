package cloud.hytora.node.impl.command;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.DefaultCommandManager;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

public class NodeCommandManager extends DefaultCommandManager {

    public NodeCommandManager() {
    }

    @Override
    protected void handleCommandChange() {
        updateIngameCommands();
    }

    @Override
    public void registerCommand(@NotNull Object command) {
        super.registerCommand(command);

        Logger.constantInstance().debug("Registered Command of Class {}", command.getClass().getName());
    }

    @Override
    public void unregisterCommand(@NotNull String name) {
        super.unregisterCommand(name);
        Logger.constantInstance().debug("Registered Command of Name {}", name);
    }

    @Override
    public void unregisterCommand(@NotNull Object instance) {

        Logger.constantInstance().debug("Registered Command of Class {}", instance.getClass().getName());
    }

    @Override
    public void unregisterCommand(@NotNull ClassLoader classLoader) {
        super.unregisterCommand(classLoader);
        Logger.constantInstance().debug("Registered Command of ClassLoader {}", classLoader);
    }

    @Override
    public @NotNull ConsoleCommandSender getThisSidesCommandSender() {
        return (ConsoleCommandSender) NodeDriver.getInstance().getCommandSender();
    }
}
