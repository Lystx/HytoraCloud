package cloud.hytora.bridge;

import cloud.hytora.bridge.proxy.ProxyRemoteHandler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.LoggingDriver;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface PluginBridge extends RemoteAdapter, LoggingDriver {

    Map<UUID, String> FIRST_JOIN_SERVER = new HashMap<>();


    default ICloudService getFirstJoinServer(UUID uuid) {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(FIRST_JOIN_SERVER.get(uuid));
    }

    default void setFirstJoinServer(UUID uuid, ICloudService service) {
        FIRST_JOIN_SERVER.put(uuid, service.getName());
    }

    default void removeFirstJoin(UUID uuid) {
        FIRST_JOIN_SERVER.remove(uuid);
    }


    default void updateServiceInfo() {
        CloudDriver.getInstance()
                .getServiceManager()
                .getThisService().onTaskSucess(cloudService -> {

                    IServiceCycleData cycleData = Remote.getInstance().createCycleData();

                    cloudService.setServiceVisibility(ServiceVisibility.VISIBLE);
                    cloudService.setServiceState(ServiceState.ONLINE);
                    if (cycleData != null) {
                        cloudService.setLastCycleData(cycleData);
                    }
                    cloudService.setReady(true);
                    cloudService.update();

                    info("==========[CloudService#UpdateServiceInfo]=========");
                    info("The ServiceInfo got updated to: [");
                    info("name={}", cloudService.getName() + ",");
                    info("uniqueId={}", cloudService.getUniqueId() + ",");
                    info("address={}", cloudService.getHostName() + ":" + cloudService.getPort() + ",");
                    info("state={}", cloudService.getServiceState() + ",");
                    info("visibility={}", cloudService.getServiceVisibility() + ",");
                    info("]");
                    info("==========[CloudService#UpdateServiceInfo]=========");

                }).onTaskFailed(e -> CloudDriver.getInstance().getLogger().error("§cCould not update ServiceInfo because service is not set yet!"));
    }

    default RemoteIdentity getIdentity() {
        return RemoteIdentity.read(new File("property.json"));
    }

    void shutdown();

    default void initialize() {
        if (Remote.getInstance().thisService().getTask().getVersion().isProxy()) {
            ProxyRemoteHandler adapter = new ProxyRemoteHandler();
            adapter.init();
        }
        CloudDriver.getInstance().getEventManager().registerListener(new CloudBridgeListener());
    }


    default void displayServerInfoStopping() {
        ICloudService cloudService = Remote.getInstance().thisService();
        cloudService.setServiceState(ServiceState.STOPPING);
        cloudService.setReady(false);
        cloudService.setServiceVisibility(ServiceVisibility.INVISIBLE);
        cloudService.update();
    }

}
