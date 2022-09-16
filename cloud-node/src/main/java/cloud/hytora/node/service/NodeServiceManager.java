package cloud.hytora.node.service;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.node.NodeDriver;

import cloud.hytora.node.config.MainConfiguration;
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
    public void registerService(ICloudServer service) {
        super.registerService(service);

        ScreenManager screenManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        screenManager.registerScreen(service.getName(), false);

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new ServiceRegisterEvent(service));

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }


    @Override
    public void unregisterService(ICloudServer service) {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new ServiceUnregisterEvent(service.getName()));
        super.unregisterService(service);

        ScreenManager screenManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        Screen screen = screenManager.getScreenByNameOrNull(service.getName());
        IServiceTask con = service.getTask();

        File parent = (con.getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        if (!service.isReady() && CloudDriver.getInstance().isRunning()) {
            NodeDriver.getInstance().getLogger().warn("Service {} probably crashed during startup because it was not authenticated when it stopped", service.getName());

            File crashFolder = new File(NodeDriver.LOG_FOLDER, "crashes/");
            crashFolder.mkdirs();

            File specificCrashFolders = new File(crashFolder, con.getName() + "/");
            specificCrashFolders.mkdirs();

            File crashFile = new File(specificCrashFolders, service.getName() + "_" + UUID.randomUUID().toString() + ".log");

            try {
                cloud.hytora.common.misc.FileUtils.writeToFile(crashFile, screen.getAllCachedLines());
                NodeDriver.getInstance().getLogger().warn("Saving logs to identify crash under {}...", crashFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                NodeDriver.getInstance().getLogger().warn("Couldn't save crash logs...");
            }
            ServiceCrashPrevention scp = MainConfiguration.getInstance().getServiceCrashPrevention();

            if (scp.isEnabled()) {

                NodeDriver.getInstance().getServiceQueue().getPausedGroups().add(con.getName());

                Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
                    NodeDriver.getInstance().getServiceQueue().getPausedGroups().remove(con.getName());
                    NodeDriver.getInstance().getServiceQueue().dequeue();
                }, scp.getTimeUnit().toMillis(scp.getTime()));

                NodeDriver.getInstance().getLogger().warn("Due to ServiceCrashPrevention (SCP) being enabled, starting services of ServiceTask {} is now paused for {} {}", con.getName(), scp.getTime(), scp.getTimeUnit().name().toLowerCase());
            }

        }

        if (con.getTaskGroup().getShutdownBehaviour().isStatic()) {
            //only delete cloud files
            File property = new File(folder, "property.json");
            property.delete();

            File bridgePlugin = new File(folder, "plugins/plugin.jar");
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

        if (screenManager.isScreenActive(screen.getName())) {
            screen.leave();
        }
        screenManager.unregisterScreen(service.getName());

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public Task<ICloudServer> startService(@NotNull ICloudServer service) {
        return worker.processService(service);
    }

    @Override
    public Task<ICloudServer> thisService() {
        return Task.empty();
    }

    @Override
    public ICloudServer thisServiceOrNull() {
        return null;
    }

    @Override
    public void sendPacketToService(@NotNull ICloudServer service, @NotNull IPacket packet) {
        NodeDriver.getInstance().getNetworkExecutor().getAllCachedConnectedClients().stream().filter(it -> it.getName().equals(service.getName())).findAny().ifPresent(it -> it.sendPacket(packet));
    }


    @Override
    public void shutdownService(ICloudServer service) {
        INode node = service.getTask().findAnyNode();
        node.stopServer(service);
    }

    @Override
    public void updateService(@NotNull ICloudServer service) {
        CloudDriver.getInstance().getLogger().debug("Updated Server {}", service.getName());
        this.updateServerInternally(service);

        //calling updateTask event on every other side
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyPacketBased(new ServiceUpdateEvent(service));
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @EventListener
    public void handleStop(ServiceUnregisterEvent event) {
        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        if (sm.isScreenActive(event.getService())) {
            sm.leaveCurrentScreen();
        }

    }

    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ICloudServer server = event.getService();
        this.updateService(server);
    }

}
