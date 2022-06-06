package cloud.hytora.bridge;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.QueryState;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;

public interface PluginBridge {

    default void bootstrap() {

        //updating service
        CloudServer service = Remote.getInstance().thisService();
        service.setServiceVisibility(ServiceVisibility.VISIBLE);
        service.setServiceState(ServiceState.ONLINE);
        service.setReady(true);
        service.update();

        CloudDriver.getInstance().getLogger().info("Service = CloudServer[name={}, port={}, state={}, visibility={}]", service.getName(), service.getPort(), service.getServiceState(), service.getServiceVisibility());

    }

    void shutdown();

}
