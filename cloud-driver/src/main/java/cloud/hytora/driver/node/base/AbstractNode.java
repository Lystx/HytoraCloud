package cloud.hytora.driver.node.base;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.DefaultNodeCycleData;
import cloud.hytora.driver.node.data.INodeCycleData;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.utils.ServiceState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractNode implements INode {

    protected INodeConfig config;

    @Setter
    protected INodeCycleData lastCycleData;


    @Override
    public @NotNull List<ICloudServer> getRunningServers() {
        return CloudDriver
                .getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .getAllCachedServices()
                .stream()
                .filter(it ->
                        it.getServiceState() == ServiceState.ONLINE &&
                            it.getRunningNodeName().equalsIgnoreCase(this.getName())
                ).collect(Collectors.toList());
    }

    @Override
    public boolean hasEnoughMemoryToStart(ICloudServer cloudServer) {
        return getConfig().getMemory() >= (getUsedMemoryByServices() + cloudServer.getTask().getMemory());
    }


    @Override
    public long getUsedMemoryByServices() {
        long memory = 0L;

        for (ICloudServer cloudServer : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllServicesByState(ServiceState.STARTING)) {
            if (!cloudServer.getRunningNodeName().equalsIgnoreCase(this.getName())) {
                continue;
            }
            memory += cloudServer.getTask().getMemory();
        }

        return memory;
    }

    @Override
    public Task<Collection<ICloudServer>> getRunningServersAsync() {
        return Task.callAsync(this::getRunningServers);
    }

    @Override
    public @NotNull String getName() {
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
    public void copy(INode from) {

    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                config = buf.readObject(DefaultNodeConfig.class);
                lastCycleData = buf.readObject(DefaultNodeCycleData.class);
                break;

            case WRITE:
                buf.writeObject(config);
                buf.writeObject(lastCycleData);
                break;
        }
    }
}
