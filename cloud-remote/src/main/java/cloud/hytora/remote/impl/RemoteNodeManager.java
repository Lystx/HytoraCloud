package cloud.hytora.remote.impl;

import cloud.hytora.driver.node.DefaultNodeManager;
import cloud.hytora.driver.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RemoteNodeManager extends DefaultNodeManager {

    @Override
    public Collection<Node> getAllNodes() {
        return getAllConnectedNodes();
    }

    @Override
    public void registerNode(@NotNull Node node) {
        this.allConnectedNodes.add(node);
    }

    @Override
    public void unRegisterNode(@NotNull Node node) {
        this.allConnectedNodes.remove(node);
    }

    @Override
    public Node getHeadNode() {
        return getAllConnectedNodes().stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }



}
