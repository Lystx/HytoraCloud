package cloud.hytora.node.service;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.packets.services.*;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.node.service.helper.ServiceQueueProcessWorker;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class NodeServiceManager extends DefaultServiceManager {

    private final Map<String, List<String>> cachedServiceOutputs = new HashMap<>();

    public NodeServiceManager() {
        // TODO: 15.04.2022 check
        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();

        executor.registerPacketHandler((PacketHandler<ServiceRequestShutdownPacket>)
                (channelHandlerContext, serviceRequestShutdownPacket) ->
                        shutdownService(CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(serviceRequestShutdownPacket.getService())));


        executor.registerPacketHandler((PacketHandler<CloudServerCacheUpdatePacket>) (ctx, packet) -> {
            ServiceInfo packetService = packet.getService();
            ServiceInfo service = getServiceByNameOrNull(packetService.getName());
            if (service == null) {
                System.out.println("Tried to update nulled service");
                return;
            }
            packetService.update();
        });
    }

    @Override
    public List<String> queryServiceOutput(ServiceInfo service) {
        return cachedServiceOutputs.getOrDefault(service.getName(), new ArrayList<>());
    }

    @Override
    public void registerService(ServiceInfo service) {
        super.registerService(service);
        this.cachedServiceOutputs.put(service.getName(), new ArrayList<>());
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new CloudServerCacheRegisterPacket(service));
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

        CloudDriver.getInstance().getEventManager().callEvent(new CloudServerCacheUnregisterEvent(service.getName()));
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new CloudServerCacheUnregisterPacket(service.getName()));

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
        }, 500).addIgnoreExceptionClass(FileSystemException.class);

    }

    public Task<ServiceInfo> startService(@NotNull ServiceInfo service) {
        return new ServiceQueueProcessWorker(this, service).processService();
    }

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
        Optional<ServiceInfo> server = this.getService(service.getName());
        if (server.isPresent()) {
            ServiceInfo serviceInfo = server.get();
            int i = allCachedServices.indexOf(serviceInfo);
            allCachedServices.set(i, service);
        }
        DriverUpdatePacket.publishUpdate(NodeDriver.getInstance());

        CloudServerCacheUpdatePacket packet = new CloudServerCacheUpdatePacket(service);
        //update all other nodes and this connected services
        NodeDriver.getInstance().getExecutor().sendPacketToAll(packet);
    }
}
