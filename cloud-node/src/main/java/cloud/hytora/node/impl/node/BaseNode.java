package cloud.hytora.node.impl.node;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.entity.services.NodeSpecificCloudService;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.entity.node.base.AbstractNode;
import cloud.hytora.driver.config.INodeConfig;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.NodeConfigManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseNode extends AbstractNode {

    private final NodeConfigManager configManager;

    public BaseNode(NodeConfigManager configManager) {
        super(configManager.universal().getNodeConfig(), DefaultNodeData.current());
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
    public List<CloudService> getRunningServers() {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> {
            s.getTask();
            return s.getTask().getPossibleNodes().contains(this.config.getNodeName());
        }).collect(Collectors.toList());
    }

    @Override
    public PacketChannel getChannel() throws IncompatibleDriverEnvironmentException {
        return CloudDriver.getInstance().getExecutor().getPacketChannel();
    }

    @Override
    public DefaultNodeData getLastCycleData() {
        return DefaultNodeData.current();
    }

    @Override
    public void setLastCycleData(INodeCycleData lastCycleData) {
    }

    @Override
    public void stopServer(CloudService server) {
        UUID id = server.getUniqueId();
        NodeSpecificCloudService processCloudServer = (NodeSpecificCloudService) server;

        //Server never connected so shutdownPacket wont work
        if (!server.isReady() || server.getServiceState() != ServiceState.ONLINE) {
            Process process = processCloudServer.getProcess();
            process.destroyForcibly();
            CloudDriver.getInstance().getServiceManager().unregisterService(server);
            return;
        }

        server.setServiceState(ServiceState.STOPPING);
        server.setServiceVisibility(ServiceVisibility.INVISIBLE);
        server.update(PublishingType.GLOBAL);

        //sending shutdown packet
        server.sendPacket(PacketCloudEntityService.forForceShutdown(server));
        //checking 4 seconds later if service has shut down or if no response
        //if no response -> force destroy the process
        CloudDriver.getInstance().getScheduledExecutor().schedule(() -> {
            Process process = processCloudServer.getProcess();
            CloudService newPossibleService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server.getName());

            UUID newestPossibleId = newPossibleService.getUniqueId();

            //if new server with same name started within this time do not destroy new process after 5 secs.
            if (!id.equals(newestPossibleId)) {
                //is same name but not same id => it means e.g Lobby-1 stopped (with e,g ID 1) and then Lobby-1 started (with e.g. ID 2) => Name is the same but internally its other server
                return;
            }
            CloudService newestSer = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server.getName());
            if (newestSer.getUniqueId().equals(id)) { //still online with old id
                CloudDriver.getInstance().getLogger().info("§cHad to force stop the Process of §e" + server.getName() + " §cbecause server did not respond!");
                process.destroyForcibly();
                CloudDriver.getInstance().getServiceManager().unregisterService(server);
            }
        }, 3, TimeUnit.SECONDS);

    }

    @Override
    public Task<NetworkResponseState> stopServerAsync(CloudService server) {
        return Task.callAsync(() -> {
            stopServer(server);
            return NetworkResponseState.OK;
        });
    }


    @Override
    public void startServer(CloudService server) {
        CloudDriver.getInstance().getServiceManager().startService(server);
    }

    @Override
    public Task<NetworkResponseState> startServerAsync(CloudService server) {
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
        this.configManager.readConfig();
        this.config = this.configManager.getConfig().getNodeConfig();
    }


}
