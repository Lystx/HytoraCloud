package cloud.hytora.driver.node.base;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class DefaultNodeManager implements INodeManager {

    protected List<INode> allCachedNodes;

    public DefaultNodeManager() {
        this.allCachedNodes = new ArrayList<>();
    }

    @Override
    public @NotNull Task<INode> getNode(@NotNull String username) {
        return Task.newInstance(getAllCachedNodes().stream().filter(n -> n.getName().equalsIgnoreCase(username)).findFirst().orElse(null));
    }

    @Override
    public @Nullable INode getNodeByNameOrNull(@NotNull String username) {
        return getAllCachedNodes().stream().filter(n -> n.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    @Override
    public boolean isHeadNode() {
        if (CloudDriver.getInstance().getEnvironment() != DriverEnvironment.NODE) {
            return false;
        }

        IHandlerNetworkExecutor executor = CloudDriver.getInstance().getNetworkExecutor();
        INode headNode = this.getHeadNode();

        if (headNode == null) {
            return false;
        }
        return headNode.getName().equalsIgnoreCase(executor.getName());
    }
}
