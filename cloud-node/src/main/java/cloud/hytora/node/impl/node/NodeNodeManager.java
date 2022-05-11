package cloud.hytora.node.impl.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.DefaultNodeManager;
import cloud.hytora.driver.node.Node;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class NodeNodeManager extends DefaultNodeManager {

    @Override
    public void registerNode(@NotNull Node node) {
        if (getNode(node.getName()).isPresent()) {
            return;
        }
        this.allConnectedNodes.add(node);
        CloudDriver.getInstance().getLogger().info("The Node '§b" + node.getName() + "§7' has joined the cluster§8!");
    }

    @Override
    public void unRegisterNode(@NotNull Node node) {
        if (getNode(node.getName()).isNull()) {
            return;
        }
        this.allConnectedNodes.remove(node);
        NodeDriver.getInstance().getLogger().info("The Node '§b" + node.getName() + "§7' has left the cluster§8!");
    }

    @Override
    public Node getHeadNode() {
        List<Node> nodesAndThisInstance = new ArrayList<>(this.allConnectedNodes);
        nodesAndThisInstance.add(NodeDriver.getInstance());
        return nodesAndThisInstance.stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }

}
