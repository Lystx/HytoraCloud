package cloud.hytora.node.impl.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.entity.node.base.DefaultNodeManager;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeRegister;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;


public class NodeNodeManager extends DefaultNodeManager {

    private final INode thisNode;

    public NodeNodeManager(INode thisNode) {
        this.thisNode = thisNode;
        this.registerNode(thisNode);
    }

    @Override
    public INode thisNode() throws IncompatibleDriverEnvironmentException {
        return thisNode;
    }

    @Override
    public void registerNode(@NotNull INode node) {
        if (getNode(node.getName()).isPresent()) {
            return;
        }
        this.allCachedNodes.add(node);
        if (node.getName().equalsIgnoreCase(CloudDriver.getInstance().getExecutor().getName())
                || getHeadNode().getName().equalsIgnoreCase(node.getName())
        ) {
            return; //if this is the current node instance do not send message
        }
        CloudDriver.getInstance().getLogger().info("§8'%1{}§8' §7has §aconnected §7to this Node§8! §8[§7address=%1{}§8]", node.getName(), node.getChannel().getClientAddress());
    }


    @Override
    public void unRegisterNode(@NotNull INode node) {
        Task<INode> cachedNode = getNode(node.getName());
        if (cachedNode.isNull()) {
            return;
        }
        this.allCachedNodes.remove(cachedNode.get());
        CloudDriver.getInstance().getLogger().info("§8'%1{}§8' §7has §cdisconnected §7from this Node§8! §8[§7address=%1{}§8]", node.getName(), node.getChannel().getClientAddress());

    }

    @Override
    public INode getHeadNode() {
        return getAllCachedNodes().stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }

}
