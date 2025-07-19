package cloud.hytora.node.service;

import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.config.IServiceCrashPrevention;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceReady;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUpdate;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.impl.DefaultServiceManager;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

import cloud.hytora.driver.config.def.UniversalNetworkConfig;
import cloud.hytora.node.service.helper.CloudServerProcessWorker;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class NodeServiceManager extends DefaultServiceManager {

    /**
     * The worker to start service
     */
    private final CloudServerProcessWorker worker;

    public NodeServiceManager() {

        this.worker = new CloudServerProcessWorker();
    }

    @Override
    public void registerService(CloudService service) {
        super.registerService(service);

        if (!service.isRunningOn(NodeDriver.getInstance().getNode())) {
            return;
        }
        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        screenManager.registerScreen(service.getName(), false);


        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }


    @Override
    public void unregisterService(CloudService service) {
        service = this.getCachedCloudService(service.getName());
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceUnregistered(service.getName()), PublishingType.GLOBAL);
        super.unregisterService(service);

        if (!service.isRunningOn(NodeDriver.getInstance().getNode())) {
            return;
        }

        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        Screen screen = screenManager.getCachedScreen(service.getName());
        ServiceTask con = service.getTask();

        File parent = (con.getTaskGroup().getShutdownBehaviour().isStatic() ? CloudDriver.Constants.SERVICE_DIR_STATIC : CloudDriver.Constants.SERVICE_DIR_DYNAMIC);
        File folder = CloudDriver.getInstance().getServerDirectoryFormatter().supply(Tuple.of(parent, service));

        if (!service.isReady() && CloudDriver.getInstance().isRunning()) {
            NodeDriver.getInstance().getLogger().warn("§8'%1" + service.getName() + "§8' §7had to be hard-stopped because %1" + service.getRunningNodeName() + " §7couldn't reach the Service§8!");
            NodeDriver.getInstance().getLogger().debug("§8=============§8[§cCrash§8]=============");
            NodeDriver.getInstance().getLogger().debug("§8=> §7Service§8: §8[§cName§8: §c{}, ID§8: §c{}§8]", service.getName(), service.getUniqueId());
            NodeDriver.getInstance().getLogger().debug("§8=> §7Explanation§8: §cProbably crashed during startup. Service was not authenticated by the Node");
            NodeDriver.getInstance().getLogger().debug("    §8=> §7by the time it was stopped. Probably didn't made it to the authentication-part§8.");

            File crashFolder = new File(CloudDriver.Constants.LOG_FOLDER, "crashes/");
            crashFolder.mkdirs();

            File specificCrashFolders = new File(crashFolder, con.getName() + "/");
            specificCrashFolders.mkdirs();

            File crashFile = new File(specificCrashFolders, service.getName() + "_" + UUID.randomUUID().toString() + ".log");

            try {
                if (screen == null) {
                    NodeDriver.getInstance().getLogger().error("§8'§c" + service.getName() + "§8' §ccould not be saved §8[§eError #0x01§8]");
                } else {
                    cloud.hytora.common.misc.FileUtils.writeToFile(crashFile, screen.getAllCachedLines());
                    NodeDriver.getInstance().getLogger().debug("§8=> §7Log saved to§8: §a{}", crashFile.getName());
                    NodeDriver.getInstance().getLogger().debug("Saving logs to identify crash under {}...", crashFile.getName());
                }
            } catch (IOException e) {
                //e.printStackTrace();
                NodeDriver.getInstance().getLogger().debug("§8=> §7Log saved to§8: §cCoudln't save §8[§e{}§8]", crashFile.getName(), e.getMessage());
            }
            IServiceCrashPrevention scp = UniversalNetworkConfig.getInstance().getServiceCrashPrevention();

            if (scp.isEnabled()) {
                NodeDriver.getInstance().getLogger().warn("§7Due to %2SCP §8[%1ServiceCrashPrevention§8] §7starting Services of this Task is disabled for §e{} {}", scp.getTime(), scp.getUnit().name());
                NodeDriver.getInstance().getServiceQueue().addPausedGroup(con.getName());

                CloudDriver.getInstance().getScheduler().scheduleDelayedTask(() -> {
                    NodeDriver.getInstance().getServiceQueue().removePausedGroup(con.getName());
                    NodeDriver.getInstance().getServiceQueue().dequeue();
                }, scp.getUnit().toMillis(scp.getTime()));
                NodeDriver.getInstance().getLogger().debug("§8=> §7SCP§8: §aEnabled §8[§7Services of this Task wont start for §e{} {}§8]", scp.getTime(), scp.getUnit().name());
            } else {
                NodeDriver.getInstance().getLogger().debug("§8=> §7SCP§8: §cDisabled §8[§7Services will start immediately§8]");
            }
            NodeDriver.getInstance().getLogger().debug("§8=============§8[§cCrash§8]=============");
        }

        if (con.getTaskGroup().getShutdownBehaviour().isStatic()) {
            //only delete cloud files
            File property = new File(folder, "property.json");
            property.delete();

            File bridgePlugin = new File(folder, "plugins/" + CloudDriver.Constants.BRIDGE_FILE_NAME);
            bridgePlugin.delete();

            File applicationFile = new File(folder, con.getVersion().getJar());
            applicationFile.delete();

        } else {
            //dynamic -> delete everything
            if (folder.exists()) {
                try {
                    FileUtils.deleteDirectory(folder);
                } catch (IOException e) {
                }
            }
        }

        screenManager.unregisterScreen(service.getName());

        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public @NotNull Task<CloudService> startService(@NotNull CloudService service) {
        return worker.processService(service);
    }

    @Override
    public CloudService thisService() throws IncompatibleDriverEnvironmentException{
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.SERVICE);
    }

    @Override
    public void sendPacketToService(CloudService service, IPacket packet) {
        PacketChannel connectedChannel = NodeDriver.getInstance().getExecutor().getConnectedChannel(service.getName());
        if (connectedChannel == null) {
            return;
        }
        connectedChannel.sendPacket(packet);
    }


    @Override
    public void shutdownService(CloudService service) {
        INode node = service.getTask().findAnyNode();
        node.stopServer(service);
    }

    @Override
    public CloudService findFallback(CloudPlayer player) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getTask().getVersion().isProxy())
                .filter(service -> service.getTask().getFallback().isEnabled())
                .filter(service -> (player.getServer() == null || !player.getServer().getName().equals(service.getName())))
                .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                .orElse(null);
    }

    @Override
    public void updateService(@NotNull CloudService service, PublishingType... type) {
        CloudDriver.getInstance().getLogger().debug("Updated Server {}", service.getName());
        PublishingType publishingType = PublishingType.get(type);

        switch (publishingType) {
            case INTERNAL:
                this.updateServerInternally(service);
                break;

            case GLOBAL:
                updateService(service, PublishingType.INTERNAL);
                updateService(service, PublishingType.PROTOCOL);
                break;
            case PROTOCOL:
                if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
                    PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
                }
                //calling update event on every other side
                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceUpdate(service), PublishingType.PROTOCOL);
                break;
        }

    }

    @EventListener
    public void handleStop(CloudEventServiceUnregistered event) {
        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);
        if (sm.isScreenActive(event.getService())) {
            sm.leaveCurrentScreen();
        }

    }

    @EventListener
    public void handleReady(CloudEventServiceReady event) {
        CloudService server = event.getCloudServer();

        ((UniversalCloudServer)server).internalReady(true);
        server.update(PublishingType.INTERNAL);
    }

    @EventListener
    public void handleUpdate(CloudEventServiceUpdate event) {
        CloudService server = event.getService();
        this.updateService(server);
    }

}
