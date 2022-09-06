package cloud.hytora.node.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.base.DefaultNodeManager;
import cloud.hytora.driver.node.INode;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;


public class NodeNodeManager extends DefaultNodeManager {

    public NodeNodeManager() {
        this.registerNode((INode) CloudDriver.getInstance().thisSidesClusterParticipant());
    }


    @Override
    public void registerNode(@NotNull INode node) {
        if (getNode(node.getName()).isPresent()) {
            return;
        }
        this.allCachedNodes.add(node);
        CloudDriver.getInstance().getLogger().info("The Node '§b" + node.getName() + "§7' has joined the cluster§8!");
    }


    @Override
    public void unRegisterNode(@NotNull INode node) {
        if (getNode(node.getName()).isNull()) {
            return;
        }
        this.allCachedNodes.remove(node);
        NodeDriver.getInstance().getLogger().info("The Node '§b" + node.getName() + "§7' has left the cluster§8!");
    }

    @Override
    public INode getHeadNode() {
        return getAllCachedNodes().stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }

}
