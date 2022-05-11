package cloud.hytora.remote.impl;

import cloud.hytora.driver.node.DefaultNodeManager;
import cloud.hytora.driver.node.Node;
import org.jetbrains.annotations.NotNull;

public class RemoteNodeManager extends DefaultNodeManager {

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
        return allConnectedNodes.stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }


}
