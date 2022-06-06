package cloud.hytora.node.impl.node;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.DefaultNodeManager;
import cloud.hytora.driver.node.Node;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class NodeNodeManager extends DefaultNodeManager {

    public NodeNodeManager() {
        this.registerNode(NodeDriver.getInstance());
    }

    @Override
    public void registerNode(@NotNull Node node) {
        if (getNode(node.getName()).isPresent()) {
            return;
        }
        this.allConnectedNodes.add(node);
        CloudDriver.getInstance().getLogger().info("The Node '§b" + node.getName() + "§7' has joined the cluster§8!");
    }

    @Override
    public List<Node> getAllConnectedNodes() {
        return super.getAllConnectedNodes().stream().filter(n -> !n.matches(NodeDriver.getInstance())).collect(Collectors.toList());
    }

    @Override
    public Collection<Node> getAllNodes() {
        List<Node> allConnectedNodes = getAllConnectedNodes();
        allConnectedNodes.add(NodeDriver.getInstance());
        return allConnectedNodes;
    }

    @Override
    public @Nullable Node getNodeByNameOrNull(@NotNull String username) {
        if (username.equalsIgnoreCase(NodeDriver.getInstance().getName())) {
            return NodeDriver.getInstance();
        }
        return super.getNodeByNameOrNull(username);
    }

    @Override
    public @NotNull Task<Node> getNode(@NotNull String username) {
        if (username.equalsIgnoreCase(NodeDriver.getInstance().getName())) {
            return Task.build(NodeDriver.getInstance());
        }
        return super.getNode(username);
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
        List<Node> nodesAndThisInstance = new ArrayList<>(this.getAllConnectedNodes());
        nodesAndThisInstance.add(NodeDriver.getInstance());
        return nodesAndThisInstance.stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }

}
