package cloud.hytora.node.node;

import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.IProcessCloudServer;
import cloud.hytora.driver.services.packet.ServiceForceShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.base.AbstractNode;
import cloud.hytora.driver.node.data.DefaultNodeCycleData;
import cloud.hytora.driver.node.data.INodeCycleData;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.config.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseNode extends AbstractNode {

    public BaseNode(ConfigManager configManager) {
        super(configManager.getConfig().getNodeConfig(), DefaultNodeCycleData.current());
    }

    @Override
    public void sendPacket(IPacket packet) {
        NodeDriver.getInstance().getNetworkExecutor().handlePacket(NodeDriver.getInstance().getNetworkExecutor().getPacketChannel(), packet);
    }


    @Override
    public ITask<Void> sendPacketAsync(IPacket packet) {
        return ITask.callAsync(() -> {
            sendPacket(packet);
            return null;
        });
    }

    @Override
    public void log(String message, Object... args) {
        NodeDriver.getInstance().getLogger().info(message, args);
    }

    @Override
    public void shutdown() {
        NodeDriver.getInstance().shutdown();
    }


    @Override
    public @NotNull List<ICloudServer> getRunningServers() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getTask().getPossibleNodes().contains(this.config.getNodeName());
        }).collect(Collectors.toList());
    }

    @Override
    public ITask<Collection<ICloudServer>> getRunningServersAsync() {
        return ITask.callAsync(() -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getRunningNodeName().equalsIgnoreCase(this.config.getNodeName());
        }).collect(Collectors.toList()));
    }

    @Override
    public @NotNull DefaultNodeCycleData getLastCycleData() {
        return DefaultNodeCycleData.current();
    }

    @Override
    public void setLastCycleData(@NotNull INodeCycleData lastCycleData) {
    }

    @Override
    public void stopServer(ICloudServer server) {
        server.sendPacket(new ServiceForceShutdownPacket(server.getName()));
        ITask.runTaskLater(() -> {
            Process process = ((IProcessCloudServer)server).getProcess();
            if (process == null) {
                return;
            }
            process.destroyForcibly();
        }, TimeUnit.MILLISECONDS, 200);
    }

    @Override
    public @NotNull ITask<NetworkResponseState> stopServerAsync(@NotNull ICloudServer server) {
        return ITask.callAsync(() -> {
            stopServer(server);
            return NetworkResponseState.OK;
        });
    }


    @Override
    public void startServer(@NotNull ICloudServer server) {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).startService(server);
    }

    @Override
    public @NotNull ITask<NetworkResponseState> startServerAsync(@NotNull ICloudServer server) {
        return ITask.callAsync(() -> {
            startServer(server);
            return NetworkResponseState.OK;
        });
    }



}
