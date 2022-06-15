package cloud.hytora.node.service;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.networking.protocol.packets.Packet;

import cloud.hytora.node.service.helper.CloudServerProcessWorker;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.*;

@Getter
public class NodeServiceManager extends DefaultServiceManager {

    /**
     * All the process output from services stored by their name
     */
    private final Map<String, List<String>> cachedServiceOutputs;

    /**
     * The worker to start service
     */
    private final CloudServerProcessWorker worker;

    public NodeServiceManager() {
        this.cachedServiceOutputs = new HashMap<>();
        this.worker = new CloudServerProcessWorker();
    }

    @Override
    public List<String> getScreenOutput(ServiceInfo service) {
        return cachedServiceOutputs.getOrDefault(service.getName(), new ArrayList<>());
    }

    @Override
    public void registerService(ServiceInfo service) {
        super.registerService(service);

        this.cachedServiceOutputs.put(service.getName(), new ArrayList<>());
        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceRegisterEvent(service));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unregisterService(ServiceInfo service) {
        super.unregisterService(service);

        ServiceTask con = service.getTask();

        File parent = (con.getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        //removing cached screen
        this.cachedServiceOutputs.remove(service.getName());

        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceUnregisterEvent(service.getName()));

        NodeDriver.getInstance().getLogger().info("§c==> §7Channel §8[§b" + service.getName() + "@" + service.getHostName() + ":" + service.getPort() + "§8] §7disconnected §8[§eUptime: " + service.getReadableUptime() + "§8]");

        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            if (!service.isReady() && CloudDriver.getInstance().isRunning()) {
                NodeDriver.getInstance().getLogger().warn("Service {} probably crashed during startup because it was not authenticated when it stopped", service.getName());

                File crashFolder = new File(NodeDriver.LOG_FOLDER, "crashes/");
                crashFolder.mkdirs();

                File specificCrashFolders = new File(crashFolder, con.getName() + "/");
                specificCrashFolders.mkdirs();

                File crashFile = new File(specificCrashFolders, service.getName() + "_" + UUID.randomUUID().toString() + ".log");

                try {
                    cloud.hytora.common.misc.FileUtils.writeToFile(crashFile, service.queryServiceOutput());
                    NodeDriver.getInstance().getLogger().warn("Saving logs to identify crash under {}...", crashFile.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    NodeDriver.getInstance().getLogger().warn("Couldn't save crash logs...");
                }
                INodeConfig config = NodeDriver.getInstance().getConfig();
                ServiceCrashPrevention scp = config.getServiceCrashPrevention();

                if (scp.isEnabled()) {

                    NodeDriver.getInstance().getServiceQueue().getPausedGroups().add(con.getName());

                    CloudDriver.getInstance().getScheduler().scheduleDelayedTask(() -> {
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
        }, 300).addIgnoreExceptionClass(FileSystemException.class);

    }

    @Override
    public Task<ServiceInfo> startService(@NotNull ServiceInfo service) {
        return worker.processService(service);
    }

    @Override
    public void sendPacketToService(ServiceInfo service, Packet packet) {
        NodeDriver.getInstance().getExecutor().getAllCachedConnectedClients().stream().filter(it -> it.getName().equals(service.getName())).findAny().ifPresent(it -> it.sendPacket(packet));
    }

    @Override
    public void shutdownService(ServiceInfo service) {
        Node node = service.getTask().findNode();
        node.stopServer(service);
    }

    @Override
    public void updateService(@NotNull ServiceInfo service) {
        this.updateServerInternally(service);

        DriverUpdatePacket.publishUpdate(NodeDriver.getInstance());

        //calling update event on every other side
        CloudDriver.getInstance().getEventManager().callEventOnlyPacketBased(new ServiceUpdateEvent(service));
    }




    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ServiceInfo server = event.getService();

        this.updateService(server);
    }

    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        String serverName = event.getService();

        Optional<ServiceInfo> service = this.getService(serverName);
        service.ifPresent(this::unregisterService);
    }

}
