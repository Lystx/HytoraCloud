package cloud.hytora.node.service;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.HytoraCloudConstants;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.node.NodeDriver;

import cloud.hytora.node.impl.config.MainConfiguration;
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
    public void registerService(ICloudService service) {
        super.registerService(service);

        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        screenManager.registerScreen(service.getName(), false);

        CloudDriver.getInstance().getEventManager().callEvent(new ServiceRegisterEvent(service), PublishingType.GLOBAL);

        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }


    @Override
    public void unregisterService(ICloudService service) {
        service = this.getCachedCloudService(service.getName());
        CloudDriver.getInstance().getEventManager().callEvent(new ServiceUnregisterEvent(service.getName()), PublishingType.GLOBAL);
        super.unregisterService(service);


        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        Screen screen = screenManager.getScreenByNameOrNull(service.getName());
        IServiceTask con = service.getTask();

        File parent = (con.getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "@" + service.getUniqueId() + "/");

        if (!service.isReady() && CloudDriver.getInstance().isRunning()) {
            NodeDriver.getInstance().getLogger().warn("§8=============§8[§cCrash§8]=============");
            NodeDriver.getInstance().getLogger().warn("§8=> §7Service§8: §8[§cName§8: §c{}, ID§8: §c{}§8]", service.getName(), service.getUniqueId());
            NodeDriver.getInstance().getLogger().warn("§8=> §7Explanation§8: §cProbably crashed during startup. Service was not authenticated by the Node");
            NodeDriver.getInstance().getLogger().warn("    §8=> §7by the time it was stopped. Probably didn't made it to the authentication-part§8.");

            File crashFolder = new File(NodeDriver.LOG_FOLDER, "crashes/");
            crashFolder.mkdirs();

            File specificCrashFolders = new File(crashFolder, con.getName() + "/");
            specificCrashFolders.mkdirs();

            File crashFile = new File(specificCrashFolders, service.getName() + "_" + UUID.randomUUID().toString() + ".log");

            try {
                cloud.hytora.common.misc.FileUtils.writeToFile(crashFile, screen.getAllCachedLines());
                NodeDriver.getInstance().getLogger().warn("§8=> §7Log saved to§8: §a{}", crashFile.getName());
                NodeDriver.getInstance().getLogger().warn("Saving logs to identify crash under {}...", crashFile.getName());
            } catch (IOException e) {
                //e.printStackTrace();
                NodeDriver.getInstance().getLogger().warn("§8=> §7Log saved to§8: §cCoudln't save §8[§e{}§8]", crashFile.getName(), e.getMessage());
            }
            ServiceCrashPrevention scp = MainConfiguration.getInstance().getServiceCrashPrevention();

            if (scp.isEnabled()) {

                NodeDriver.getInstance().getServiceQueue().getPausedGroups().add(con.getName());

                CloudDriver.getInstance().getScheduler().scheduleDelayedTask(() -> {
                    NodeDriver.getInstance().getServiceQueue().getPausedGroups().remove(con.getName());
                    NodeDriver.getInstance().getServiceQueue().dequeue();
                }, scp.getTimeUnit().toMillis(scp.getTime()));
                NodeDriver.getInstance().getLogger().warn("§8=> §7SCP§8: §aEnabled §8[§7Services of this Task wont start for §e{} {}§8]", scp.getTime(), scp.getTimeUnit().name());
            } else {
                NodeDriver.getInstance().getLogger().warn("§8=> §7SCP§8: §cDisabled §8[§7Services will start immediately§8]");
            }
            NodeDriver.getInstance().getLogger().warn("§8=============§8[§cCrash§8]=============");
        }

        if (con.getTaskGroup().getShutdownBehaviour().isStatic()) {
            //only delete cloud files
            File property = new File(folder, "property.json");
            property.delete();

            File bridgePlugin = new File(folder, "plugins/" + HytoraCloudConstants.BRIDGE_FILE_NAME);
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
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public Task<ICloudService> startService(@NotNull ICloudService service) {
        return worker.processService(service);
    }

    @Override
    public Task<ICloudService> getThisService() {
        return Task.empty();
    }

    @Override
    public ICloudService thisService() {
        return null;
    }

    @Override
    public void sendPacketToService(ICloudService service, IPacket packet) {
        NodeDriver.getInstance().getExecutor().getAllCachedConnectedClients().stream().filter(it -> it.getName().equals(service.getName())).findAny().ifPresent(it -> it.sendPacket(packet));
    }


    @Override
    public void shutdownService(ICloudService service) {
        INode node = service.getTask().findAnyNode();
        node.stopServer(service);
    }

    @Override
    public ICloudService findFallback(ICloudPlayer player) {
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
    public void updateService(@NotNull ICloudService service, PublishingType... type) {
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
                    DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor());
                }
                //calling update event on every other side
                CloudDriver.getInstance().getEventManager().callEvent(new ServiceUpdateEvent(service), PublishingType.PROTOCOL);
                break;
        }

    }

    @EventListener
    public void handleStop(ServiceUnregisterEvent event) {
        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);
        if (sm.isScreenActive(event.getService())) {
            sm.leaveCurrentScreen();
        }

    }

    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ICloudService server = event.getService();
        this.updateService(server);
    }

}
