package cloud.hytora.node.service;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.networking.packets.services.*;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.node.service.helper.ProcessServiceStarter;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            CloudServer packetService = packet.getService();
            CloudServer service = getServiceByNameOrNull(packetService.getName());
            if (service == null) {
                return;
            }
            service.cloneInternally(packetService, service);
            service.update();
        });
    }

    @Override
    public List<String> queryServiceOutput(CloudServer service) {
        return cachedServiceOutputs.getOrDefault(service.getName(), new ArrayList<>());
    }

    @Override
    public void registerService(CloudServer service) {
        super.registerService(service);
        this.cachedServiceOutputs.put(service.getName(), new ArrayList<>());
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new CloudServerCacheRegisterPacket(service));
    }

    @Override
    public void unregisterService(CloudServer service) {
        super.unregisterService(service);
        this.cachedServiceOutputs.remove(service.getName());

        ServerConfiguration con = service.getConfiguration();

        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");


        if (folder.exists()) {
            try {
                FileUtils.deleteDirectory(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CloudDriver.getInstance().getEventManager().callEvent(new CloudServerCacheUnregisterEvent(service.getName()));
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new CloudServerCacheUnregisterPacket(service.getName()));

        String uptime = StringUtils.getReadableMillisDifference(service.getCreationTimestamp(), System.currentTimeMillis());
        NodeDriver.getInstance().getLogger().info("§c==> §7Channel §8[§b" + service.getName() + "@" + service.getHostName() + ":" + service.getPort() + "§8] §7disconnected §8[§eUptime: " + uptime+ "§8]");
    }

    public Wrapper<CloudServer> startService(@NotNull CloudServer service) {
        return new ProcessServiceStarter(this,service).start();
    }

    public void sendPacketToService(CloudServer service, Packet packet) {
        NodeDriver.getInstance().getExecutor().getAllCachedConnectedClients().stream().filter(it -> it.getName().equals(service.getName())).findAny().ifPresent(it -> it.sendPacket(packet));
    }

    @Override
    public void shutdownService(CloudServer service) {
        if (service.getConfiguration().getNode().equals(NodeDriver.getInstance().getExecutor().getNodeName())) {
            this.sendPacketToService(service, new ServiceShutdownPacket(service.getName()));
        } else {
            //TODO
        }
    }


    @Override
    public void updateService(@NotNull CloudServer service) {
        this.getService(service.getName()).ifPresent(ser -> {
            int i = allCachedServices.indexOf(ser);
            allCachedServices.set(i, service);
        });

        CloudServerCacheUpdatePacket packet = new CloudServerCacheUpdatePacket(service);
        //update all other nodes and this connected services
        NodeDriver.getInstance().getExecutor().sendPacketToAll(packet);
    }
}
