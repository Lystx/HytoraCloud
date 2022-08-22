package cloud.hytora.driver.commands.events;

import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.event.CloudEvent;
import lombok.Data;

@Data
public class CommandRegisterEvent implements CloudEvent {

    private final DriverCommand command;
}
