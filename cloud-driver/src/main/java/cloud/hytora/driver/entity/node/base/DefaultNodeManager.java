package cloud.hytora.driver.entity.node.base;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeRegister;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeUnregister;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeUpdate;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public abstract class DefaultNodeManager implements NodeManager {

    protected List<INode> allCachedNodes;

    public DefaultNodeManager() {
        this.allCachedNodes = new ArrayList<>();
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }


    @EventListener
    public void handleUpdate(CloudEventNodeUpdate event) {
        INode node = event.getNode();

        this.updateNode(node, PublishingType.INTERNAL);
    }

    @EventListener
    public void handleRegister(CloudEventNodeRegister event) {
        INode node = event.getNode();
        System.out.println("Register Event " + node.getName());
        this.registerNode(node);
    }

    @EventListener
    public void handleUnregister(CloudEventNodeUnregister event) {
        INode node = event.getNode();
        this.unRegisterNode(node);
    }


    @Override
    public INode thisNode() throws IncompatibleDriverEnvironmentException {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    @Override
    public @NotNull Task<INode> getNode(@NotNull String username) {
        return getAllCachedNodes().stream().filter(n -> n.getName().equalsIgnoreCase(username)).findFirst().map(Task::build).orElse(Task.empty());
    }

    @Override
    public @NotNull Task<INode> getNode(@NotNull UUID uniqueId) {
        return getAllCachedNodes().stream().filter(n -> n.getConfig().getUniqueId().equals(uniqueId)).findFirst().map(Task::build).orElse(Task.empty());
    }

    @Override
    public @Nullable INode getCachedNode(@NotNull String username) {
        return getNode(username).orElse(null);
    }
    @Override
    public @Nullable INode getCachedNode(@NotNull UUID uniqueId) {
        return getNode(uniqueId).orElse(null);
    }

    @Override
    public void updateNode(INode node, PublishingType... type) {

        CloudDriver.getInstance().getLogger().debug("Updated Node {}", node.getName());
        PublishingType publishingType = PublishingType.get(type);

        switch (publishingType) {
            case INTERNAL:
                INode cachedNode = this.getCachedNode(node.getName());
                if (cachedNode != null) {
                    int i = allCachedNodes.indexOf(cachedNode);
                    allCachedNodes.set(i, cachedNode);
                } else {
                    allCachedNodes.add(node);
                }
                break;

            case GLOBAL:
                updateNode(node, PublishingType.INTERNAL);
                updateNode(node, PublishingType.PROTOCOL);
                break;
            case PROTOCOL:
                //calling update event on every other side
                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventNodeUpdate(node), PublishingType.PROTOCOL);
                break;
        }

    }

    @Override
    public boolean isHeadNode() {
        if (CloudDriver.getInstance().getEnvironment() != CloudDriver.Environment.NODE) {
            return false;
        }

        HandlingNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        INode headNode = this.getHeadNode();

        if (headNode == null) {
            return false;
        }
        return headNode.getName().equalsIgnoreCase(executor.getName());
    }
}
