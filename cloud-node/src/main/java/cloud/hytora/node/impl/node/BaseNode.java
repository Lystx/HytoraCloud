package cloud.hytora.node.impl.node;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.IProcessCloudServer;
import cloud.hytora.driver.services.packet.ServiceForceShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.base.AbstractNode;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.DefaultNodeData;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.ConfigManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseNode extends AbstractNode {

    private final ConfigManager configManager;
    public BaseNode(ConfigManager configManager) {
        super(configManager.getConfig().getNodeConfig(), DefaultNodeData.current());
        this.configManager = configManager;
    }

    @Override
    public void sendPacket(IPacket packet) {
        NodeDriver.getInstance().getExecutor().handlePacket(NodeDriver.getInstance().getExecutor().getPacketChannel(), packet);
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
    public List<ICloudService> getRunningServers() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getTask().getPossibleNodes().contains(this.config.getNodeName());
        }).collect(Collectors.toList());
    }

    @Override
    public Task<Collection<ICloudService>> getRunningServersAsync() {
        return Task.callAsync(() -> CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getRunningNodeName().equalsIgnoreCase(this.config.getNodeName());
        }).collect(Collectors.toList()));
    }

    @Override
    public DefaultNodeData getLastCycleData() {
        return DefaultNodeData.current();
    }

    @Override
    public void setLastCycleData(INodeData lastCycleData) {
    }

    @Override
    public void stopServer(ICloudService server) {
        UUID id = server.getUniqueId();
        IProcessCloudServer processCloudServer = (IProcessCloudServer)server;


        //Server never connected so shutdownPacket wont work
        if (!server.isReady()) {
            Process process = processCloudServer.getProcess();
            process.destroyForcibly();
            CloudDriver.getInstance().getServiceManager().unregisterService(server);
            CloudDriver.getInstance().getLogger().info("§cHad to force stop the Process of §e" + server.getName() +"§c!");
            return;
        }
        //sending shutdown packet
        server.sendPacket(new ServiceForceShutdownPacket(server.getName()));

        //checking 3 seconds later if service has shut down or if no response
        //if no response -> force destroy the process
        Task.runTaskLater(() -> {
            Process process = processCloudServer.getProcess();
            ICloudService newPossibleService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server.getName());
            if (process == null || !process.isAlive() || newPossibleService == null) {
                return;
            }
            UUID newestPossibleId = newPossibleService.getUniqueId();

            //if new server with same name started within this time do not destroy new process after 5 secs.
            if (!id.equals(newestPossibleId)) {
                return;
            }
            CloudDriver.getInstance().getLogger().info("§cHad to force stop the Process of §e" + server.getName() +"§c!");
            process.destroyForcibly();
        }, TimeUnit.SECONDS, 3);
    }

    @Override
    public Task<NetworkResponseState> stopServerAsync(ICloudService server) {
        return Task.callAsync(() -> {
            stopServer(server);
            return NetworkResponseState.OK;
        });
    }


    @Override
    public void startServer(ICloudService server) {
        CloudDriver.getInstance().getServiceManager().startService(server);
    }

    @Override
    public Task<NetworkResponseState> startServerAsync(ICloudService server) {
        return Task.callAsync(() -> {
            startServer(server);
            return NetworkResponseState.OK;
        });
    }


    public void setNodeConfig(INodeConfig config) {
        this.config = config;
    }

    public void loadConfig() throws Exception {
        //loading config
        this.configManager.read();
        this.config = this.configManager.getConfig().getNodeConfig();
    }


}
