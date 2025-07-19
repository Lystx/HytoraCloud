package cloud.hytora.driver.entity.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface NodeManager {

    /**
     * All connected nodes
     */
    List<INode> getAllCachedNodes();


    void setAllCachedNodes(List<INode> nodes);

    @NotNull Task<INode> getNode(@NotNull String username);

    @NotNull Task<INode> getNode(@NotNull UUID uniqueId);

    @Nullable INode getCachedNode(@NotNull String username);
    @Nullable INode getCachedNode(@NotNull UUID uniqueId);

    void registerNode(@NotNull INode node);

    void unRegisterNode(@NotNull INode node);

    /**
     * The head node that manages everything
     */
    INode getHeadNode();

    INode thisNode() throws IncompatibleDriverEnvironmentException;

    /**
     * If the current process is the headNode
     */
    boolean isHeadNode();

    void updateNode(INode abstractNode, PublishingType... publishingTypes);
}
