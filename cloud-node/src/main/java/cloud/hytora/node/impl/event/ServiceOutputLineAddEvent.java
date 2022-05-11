package cloud.hytora.node.impl.event;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.services.CloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServiceOutputLineAddEvent implements CloudEvent {

    private final CloudServer service;
    private final String line;
}
