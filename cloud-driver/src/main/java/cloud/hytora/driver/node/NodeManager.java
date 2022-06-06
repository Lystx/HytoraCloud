package cloud.hytora.driver.node;

import cloud.hytora.common.wrapper.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface NodeManager {

    /**
     * All connected nodes (excluding the own if executed on node-side)
     */
    List<Node> getAllConnectedNodes();

    /**
     * All nodes including the main node
     */
    Collection<Node> getAllNodes();

    void setAllConnectedNodes(List<Node> nodes);

    @NotNull Task<Node> getNode(@NotNull String username);

    @Nullable Node getNodeByNameOrNull(@NotNull String username);

    void registerNode(@NotNull Node node);

    void unRegisterNode(@NotNull Node node);

    /**
     * The head node that manages everything
     */
    Node getHeadNode();

    /**
     * If the current process is the headNode
     */
    boolean isHeadNode();
}
