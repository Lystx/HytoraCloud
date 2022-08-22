package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;

public class NodeParamType extends CommandParameterType<INode> {

    @Override
    public String label() {
        return "node";
    }

    @Override
    public INode resolve(String s) {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getNodeByNameOrNull(s);
    }

    @Override
    public Class<INode> typeClass() {
        return INode.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        return true;
    }

    @Override
    public String handleCustomException(String s) {
        return null;
    }
}
