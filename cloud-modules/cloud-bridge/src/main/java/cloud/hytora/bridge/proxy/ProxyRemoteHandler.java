package cloud.hytora.bridge.proxy;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;

import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.remote.Remote;

import java.util.List;

public class ProxyRemoteHandler {

    public ProxyRemoteHandler() {


        //register events
        CloudDriver.getInstance().getEventManager().registerListener(this);

    }

    public void init() {

        //load all current groups
        for (ICloudService allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            IServiceTask serviceGroup = allCachedService.getTask();
            if (!serviceGroup.getVersion().isProxy()) {
                Remote.getInstance().getProxyAdapter().registerService(allCachedService);
            }
        }
    }

    @EventListener
    public void handle(TaskMaintenanceChangeEvent event) {
        IServiceTask task = event.getTask();
        CloudMessages cloudMessages = CloudDriver.getInstance().getStorage().get("cloud::messages").toInstance(CloudMessages.class);

        ICloudService thisService = CloudDriver.getInstance().getServiceManager().thisService();

        if (event.isNewMaintenanceValue() && task.getName().equalsIgnoreCase(thisService.getTask().getName())) {

            List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);
            for (ICloudPlayer cp : thisService.getOnlinePlayers()) {
                if (whitelistedPlayers.contains(cp.getName())) {
                    PlayerExecutor.forPlayer(cp).sendMessage(cloudMessages.getMaintenanceKickByPassedMessage());
                    continue;
                }
                PlayerExecutor.forPlayer(cp).disconnect(cloudMessages.getNetworkCurrentlyInMaintenance());
            }

        }
    }


    @EventListener
    public void handle(ServiceRegisterEvent event) {
        ICloudService cloudServer = event.getCloudServer();
        if (cloudServer.getTask() == null) {
            return;
        }
        if (!cloudServer.getTask().getVersion().isProxy()) {
            Remote.getInstance().getProxyAdapter().registerService(cloudServer);
        }
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {
        Remote.getInstance().getProxyAdapter().unregisterService(event.getCloudServer());
    }

    @EventListener
    public void handle(DriverCacheUpdateEvent event) {
        Remote.getInstance().getProxyAdapter().clearServices();
        for (ICloudService service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (!service.getTask().getVersion().isProxy()) {
                Remote.getInstance().getProxyAdapter().registerService(service);
            }
        }
    }
}
