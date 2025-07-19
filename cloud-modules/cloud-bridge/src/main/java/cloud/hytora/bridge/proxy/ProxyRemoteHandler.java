package cloud.hytora.bridge.proxy;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverCacheUpdate;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceRegistered;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;

import cloud.hytora.driver.event.defaults.task.CloudEventTaskMaintenance;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.remote.Remote;

import java.util.ArrayList;
import java.util.List;

public class ProxyRemoteHandler {

    public ProxyRemoteHandler() {
        //register events
        CloudDriver.getInstance().getEventManager().registerListener(this);

    }

    public void init() {

        //load all current groups
        for (CloudService allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            ServiceTask serviceGroup = allCachedService.getTask();
            if (!serviceGroup.getVersion().isProxy()) {
                Remote.getInstance().getProxyAdapter().registerService(allCachedService);
            }
        }
    }

    @EventListener
    public void handle(CloudEventTaskMaintenance event) {
        ServiceTask task = event.getTask();
        UniversalCloudMessages cloudMessages = CloudDriver.getInstance().getConfigManager().getConfig().getMessages();

        CloudService thisService = CloudDriver.getInstance().getServiceManager().thisService();

        if (event.isNewMaintenanceValue() && task.getName().equalsIgnoreCase(thisService.getTask().getName())) {

            List<String> whitelistedPlayers = new ArrayList<>(CloudDriver.getInstance().getConfigManager().getConfig().getWhitelistedPlayers());
            for (CloudPlayer cp : thisService.getOnlinePlayers()) {
                if (whitelistedPlayers.contains(cp.getName()) || cp.hasPermission("cloud.maintenance.bypass")) {
                    cp.sendMessage(cloudMessages.getMaintenanceKickByPassedMessage());
                    continue;
                }
                cp.asProxyPlayer().disconnect(cloudMessages.getNetworkCurrentlyInMaintenance());
            }

        }
    }


    @EventListener
    public void handle(CloudEventServiceRegistered event) {
        CloudService cloudServer = event.getCloudServer();
        if (cloudServer.getTask() == null) {
            return;
        }
        if (!cloudServer.getTask().getVersion().isProxy()) {
            Remote.getInstance().getProxyAdapter().registerService(cloudServer);
        }
    }

    @EventListener
    public void handle(CloudEventServiceUnregistered event) {
        Remote.getInstance().getProxyAdapter().unregisterService(event.getCloudServer());
    }

    @EventListener
    public void handle(CloudEventDriverCacheUpdate event) {
        Remote.getInstance().getProxyAdapter().clearServices();
        for (CloudService service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (!service.getTask().getVersion().isProxy()) {
                Remote.getInstance().getProxyAdapter().registerService(service);
            }
        }
    }
}
