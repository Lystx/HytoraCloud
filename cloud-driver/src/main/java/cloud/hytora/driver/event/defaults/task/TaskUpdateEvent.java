package cloud.hytora.driver.event.defaults.task;

import cloud.hytora.driver.event.CloudEvent;

import cloud.hytora.driver.services.task.ServiceTask;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskUpdateEvent implements CloudEvent {

    private final ServiceTask configuration;


}
