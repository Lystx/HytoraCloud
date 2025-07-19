package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.Cancelable;
import cloud.hytora.driver.event.LocalEvent;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class CancelableEvent implements LocalEvent, Cancelable {


    private boolean cancelled;
}
