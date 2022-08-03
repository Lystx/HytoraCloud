package cloud.hytora.driver.component.event.hover;

import cloud.hytora.driver.component.event.ComponentEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class HoverEvent implements ComponentEvent<HoverEvent> {

    /**
     * The action (e.g. open url)
     */
    private final HoverAction type;

    /**
     * The value provided for the type
     */
    private final String value;

    @Override
    public HoverEvent copy() {
        return new HoverEvent(type, value);
    }
}
