package cloud.hytora.node.impl.command;

import cloud.hytora.driver.commands.AbstractCommandManager;
import cloud.hytora.driver.commands.parameter.IParameterTypeRegistry;

public class NodeCommandManager extends AbstractCommandManager {

    public NodeCommandManager() {
    }

    @Override
    public void handleCommandChange() {
        updateIngameCommands();
    }

}
