package cloud.hytora.node.commands;

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
