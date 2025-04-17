package cloud.hytora.driver.event;

import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class CloudEventListener<T extends CloudEvent> {

    private final Class<T> typeClass;
    private final Consumer<T> handler;

    @EventListener
    public void handle(T event) {
        if (event.getClass().getName().equalsIgnoreCase(typeClass.getName())) {
            this.handler.accept(event);
        }
    }
}
