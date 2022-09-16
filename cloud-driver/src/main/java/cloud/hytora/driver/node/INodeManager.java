package cloud.hytora.driver.node;

import cloud.hytora.common.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface INodeManager {

    /**
     * All connected nodes
     */
    List<INode> getAllCachedNodes();


    void setAllCachedNodes(List<INode> nodes);

    @NotNull Task<INode> getNode(@NotNull String username);

    @Nullable INode getNodeByNameOrNull(@NotNull String username);

    void registerNode(@NotNull INode node);

    void unRegisterNode(@NotNull INode node);

    /**
     * The head node that manages everything
     */
    INode getHeadNode();

    /**
     * If the current process is the headNode
     */
    boolean isHeadNode();
}
