package cloud.hytora.driver.event.defaults.task;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.services.task.ServiceTask;
import lombok.*;

@AllArgsConstructor
@Getter
public class TaskMaintenanceChangeEvent implements CloudEvent {

    private final ServiceTask task;
    private final boolean newMaintenanceValue;

}
