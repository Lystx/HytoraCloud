package cloud.hytora.bridge;

import cloud.hytora.bridge.proxy.ProxyRemoteHandler;
import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.LoggingDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceCycleData;
import cloud.hytora.driver.entity.services.utils.RemoteIdentity;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface PluginBridge extends RemoteAdapter, LoggingDriver {

    Map<UUID, String> FIRST_JOIN_SERVER = new HashMap<>();


    default CloudService getFirstJoinServer(UUID uuid) {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(FIRST_JOIN_SERVER.get(uuid));
    }

    default void setFirstJoinServer(UUID uuid, CloudService service) {
        FIRST_JOIN_SERVER.put(uuid, service.getName());
    }

    default void removeFirstJoin(UUID uuid) {
        FIRST_JOIN_SERVER.remove(uuid);
    }


    default void updateServiceInfo() {
        CloudService cloudService = CloudDriver.getInstance()
                .getServiceManager().thisService();
        if (cloudService == null) {
            CloudDriver.getInstance().getLogger().error("§cCould not update ServiceInfo because service is not set yet!");
            return;
        }

        ServiceCycleData cycleData = Remote.getInstance().createCycleData();

        cloudService.setServiceVisibility(ServiceVisibility.VISIBLE);
        cloudService.setServiceState(ServiceState.ONLINE);
        if (cycleData != null) {
            cloudService.setLastCycleData(cycleData);
        }
        cloudService.setReady(true);
        cloudService.update();

        System.out.println(" ");
        DriverUtility.printColored("Bridge", "§7Service is aware of itsself §8[%1identification§8=%2" + cloudService.getName() + "§8@%2" + cloudService.getUniqueId() + "§8,%1state§8=%2" + cloudService.getServiceState() + "§8,%1visibility§8=%2" + cloudService.getServiceVisibility() + "§8]");
        System.out.println(" ");
    }

    default RemoteIdentity getIdentity() {
        return RemoteIdentity.read(new File("property.json"));
    }

    @Override
    Task<Boolean> shutdown();

    default void initialize() {
        if (Remote.getInstance().thisService().getTask().getVersion().isProxy()) {
            ProxyRemoteHandler adapter = new ProxyRemoteHandler();
            adapter.init();
        }
        CloudDriver.getInstance().getEventManager().registerListener(new CloudBridgeListener());
    }


    default void disconnectConnection(UUID connectionId) {

    }

    default void displayServerInfoStopping() {
        CloudService cloudService = Remote.getInstance().thisService();
        cloudService.setServiceState(ServiceState.STOPPING);
        cloudService.setServiceVisibility(ServiceVisibility.INVISIBLE);
        cloudService.update();
    }

}
