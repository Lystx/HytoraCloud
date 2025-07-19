package cloud.hytora.driver.event.listener;

import cloud.hytora.driver.event.LocalEvent;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class CloudEventListener<T extends LocalEvent> {

    private final Class<T> typeClass;
    private final Consumer<T> handler;

    @EventListener
    public void handle(T event) {
        if (event.getClass().getName().equalsIgnoreCase(typeClass.getName())) {
            this.handler.accept(event);
        }
    }
}
