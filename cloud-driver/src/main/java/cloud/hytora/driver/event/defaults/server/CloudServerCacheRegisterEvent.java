package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.services.CloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class CloudServerCacheRegisterEvent implements CloudEvent {

    private final CloudServer server;

}
