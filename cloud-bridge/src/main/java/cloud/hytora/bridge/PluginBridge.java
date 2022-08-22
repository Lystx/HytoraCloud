package cloud.hytora.bridge;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;

import java.io.File;

public interface PluginBridge {

    default void bootstrap() {
        //updating service
        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .thisService()
                .onTaskSucess(cloudServer -> {
                    cloudServer.setServiceVisibility(ServiceVisibility.VISIBLE);
                    cloudServer.setServiceState(ServiceState.ONLINE);
                    cloudServer.setReady(true);
                    cloudServer.update();
                    CloudDriver.getInstance().getLogger().info("Service = CloudServer[name={}, port={}, state={}, visibility={}]", cloudServer.getName(), cloudServer.getPort(), cloudServer.getServiceState(), cloudServer.getServiceVisibility());
                }).onTaskFailed(e -> {
                    System.out.println("HUGE MISTAKE");
                });
    }

    default RemoteIdentity getIdentity() {
        return RemoteIdentity.read(new File("property.json"));
    }

    void shutdown();

}
