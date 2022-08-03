package cloud.hytora.driver.component.event;

import cloud.hytora.driver.component.event.click.ClickAction;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverAction;
import cloud.hytora.driver.component.event.hover.HoverEvent;

public interface ComponentEvent<E extends ComponentEvent<E>> {

    /**
     * Creates a new {@link ComponentEvent} that refers to the {@link HoverEvent}
     *
     * @param action the action for the event
     * @param value the value for the event
     * @return created event
     */
    static ComponentEvent<HoverEvent> hover(HoverAction action, String value) {
        return new HoverEvent(action, value);
    }

    /**
     * Creates a new {@link ComponentEvent} that refers to the {@link ClickEvent}
     *
     * @param action the action for the event
     * @param value the value for the event
     * @return created event
     */
    static ComponentEvent<ClickEvent> click(ClickAction action, String value) {
        return new ClickEvent(action, value);
    }

    /**
     * The event type
     */
    Enum<?> getType();

    /**
     * The value of the event
     */
    String getValue();

    /**
     * Copies the current {@link ComponentEvent}
     *
     * @return generic instance copied
     */
    E copy();

}
