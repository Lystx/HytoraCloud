package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.CloudEvent;

import cloud.hytora.driver.services.configuration.ServerConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloudServiceGroupUpdateEvent implements CloudEvent {

    private final ServerConfiguration configuration;


}
