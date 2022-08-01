package cloud.hytora.remote.impl;

import cloud.hytora.driver.node.base.DefaultNodeManager;
import cloud.hytora.driver.node.INode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RemoteNodeManager extends DefaultNodeManager {

    @Override
    public void registerNode(@NotNull INode node) {
        this.allCachedNodes.add(node);
    }

    @Override
    public void unRegisterNode(@NotNull INode node) {
        this.allCachedNodes.remove(node);
    }

    @Override
    public INode getHeadNode() {
        return getAllCachedNodes().stream().filter(node -> !node.getConfig().isRemote()).findFirst().orElse(null);
    }



}
