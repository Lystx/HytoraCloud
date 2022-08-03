package cloud.hytora.driver.component.event.click;

import cloud.hytora.driver.component.event.ComponentEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class ClickEvent implements ComponentEvent<ClickEvent> {

    /**
     * The action (e.g. open url)
     */
    private final ClickAction type;

    /**
     * The value provided for the type
     */
    private final String value;

    @Override
    public ClickEvent copy() {
        return new ClickEvent(type, value);
    }
}
