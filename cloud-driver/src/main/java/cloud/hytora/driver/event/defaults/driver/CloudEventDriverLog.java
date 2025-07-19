package cloud.hytora.driver.event.defaults.driver;

import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.driver.event.LocalEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CloudEventDriverLog implements LocalEvent {

    private LogEntry entry;
}
