package cloud.hytora.bridge;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;

public interface PluginBridge {

    default void bootstrap() {

        //updating service
        ServiceInfo service = Remote.getInstance().thisService();
        service.setServiceVisibility(ServiceVisibility.VISIBLE);
        service.setServiceState(ServiceState.ONLINE);
        service.setReady(true);
        service.update();

        CloudDriver.getInstance().getLogger().info("Service = CloudServer[name={}, port={}, state={}, visibility={}]", service.getName(), service.getPort(), service.getServiceState(), service.getServiceVisibility());

    }

    void shutdown();

}
