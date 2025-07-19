package cloud.hytora.modules.sign.cloud.listener;

import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceReady;
import cloud.hytora.modules.sign.api.CloudSignAPI;

public class ModuleServiceReadyListener {

    @EventListener
    public void handle(CloudEventServiceReady event) {
        CloudSignAPI.getInstance().getSignManager().update();
        CloudSignAPI.getInstance().publishConfiguration();
    }
}
