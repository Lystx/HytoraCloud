package cloud.hytora.node.impl.event;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.services.ServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServiceOutputLineAddEvent implements CloudEvent {

    private final ServiceInfo service;
    private final String line;
}
