package cloud.hytora.driver.event.defaults.driver;

import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.driver.event.CloudEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DriverLogEvent implements CloudEvent {

    private LogEntry entry;
}
