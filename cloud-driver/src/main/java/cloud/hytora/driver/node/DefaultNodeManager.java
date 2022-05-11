package cloud.hytora.driver.node;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class DefaultNodeManager implements NodeManager {

    protected List<Node> allConnectedNodes;

    public DefaultNodeManager() {
        this.allConnectedNodes = new ArrayList<>();
    }

    @Override
    public @NotNull Wrapper<Node> getNode(@NotNull String username) {
        return Wrapper.build(getAllConnectedNodes().stream().filter(n -> n.getName().equalsIgnoreCase(username)).findFirst().orElse(null));
    }

    @Override
    public @Nullable Node getNodeByNameOrNull(@NotNull String username) {
        return getAllConnectedNodes().stream().filter(n -> n.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    @Override
    public boolean isHeadNode() {
        if (CloudDriver.getInstance().getEnvironment() != DriverEnvironment.NODE) {
            return false;
        }

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        Node headNode = this.getHeadNode();

        if (headNode == null) {
            return false;
        }
        return headNode.getName().equalsIgnoreCase(executor.getName());
    }
}
