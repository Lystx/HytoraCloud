package cloud.hytora.modules.sign.cloud.listener;

import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.modules.sign.api.CloudSignAPI;

public class ModuleServiceReadyListener {

    @EventListener
    public void handle(ServiceReadyEvent event) {
        CloudSignAPI.getInstance().getSignManager().update();
        CloudSignAPI.getInstance().publishConfiguration();
    }
}
