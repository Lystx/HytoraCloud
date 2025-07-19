package cloud.hytora.driver.entity.node.base;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.config.def.UniversalNodeConfig;
import cloud.hytora.driver.config.INodeConfig;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractNode implements INode {

    protected INodeConfig config;

    @Setter
    protected INodeCycleData lastCycleData;


    @Override
    public void update(PublishingType... publishingTypes) {
        CloudDriver.getInstance().getNodeManager().updateNode(this, publishingTypes);
    }

    @Override
    public Collection<CloudService> getAssignedServers() {
        return CloudDriver.getInstance()
                .getServiceManager()
                .getAllCachedServices()
                .stream()
                .filter(it -> it.getRunningNodeName().equalsIgnoreCase(this.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CloudService> getRunningServers() {
        return getAssignedServers()
                .stream()
                .filter(it -> it.getServiceState() == ServiceState.ONLINE)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasEnoughMemoryToStart(CloudService cloudServer) {
        return getConfig().getMemory() >= (getUsedMemoryByServices() + cloudServer.getTask().getMemory());
    }


    @Override
    public long getUsedMemoryByServices() {
        long memory = 0L;

        for (CloudService cloudServer : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (!cloudServer.getRunningNodeName().equalsIgnoreCase(this.getName())) {
                continue;
            }
            memory += cloudServer.getTask().getMemory();
        }

        return memory;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof INode) {
            INode match = (INode) o;
            return match.getName().equalsIgnoreCase(this.getName()) && match.getConfig().getUniqueId().equals(this.getConfig().getUniqueId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, lastCycleData);
    }

    @Override
    public String getName() {
        return this.config.getNodeName();
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.NODE;
    }

    @Override
    public String getMainIdentity() {
        return this.config.getUniqueId().toString();
    }

    @Override
    public String replacePlaceHolders(String input) {
        return input;
    }

    @Override
    public void clone(INode from) {

    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                config = buf.readObject(UniversalNodeConfig.class);
                lastCycleData = buf.readObject(DefaultNodeData.class);
                break;

            case WRITE:
                buf.writeObject(config);
                buf.writeObject(lastCycleData);
                break;
        }
    }
}
