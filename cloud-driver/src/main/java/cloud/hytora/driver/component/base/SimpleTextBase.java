package cloud.hytora.driver.component.base;

import cloud.hytora.driver.component.feature.ComponentOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SimpleTextBase<B extends TextBased<B>> implements TextBased<B> {

    /**
     * All the stored options to this base
     */
    protected Map<ComponentOption<B, ?>, Object> options;

    /**
     * All handlers when adding an option
     */
    private final List<Consumer<ComponentOption<B, ?>>> optionHandler;

    /**
     * Constructs this simple base
     */
    public SimpleTextBase() {
        this.options = new ConcurrentHashMap<>();
        this.optionHandler = new ArrayList<>();
    }

    @Override
    public <T> B put(ComponentOption<B, T> option, T t) {
        option.setValue(t);
        this.options.put(option, t);
        for (Consumer<ComponentOption<B, ?>> componentOptionConsumer : this.optionHandler) {
            componentOptionConsumer.accept(option);
        }
        return (B) this;
    }

    /**
     * Adds a handler to this base to listen for options
     */
    public void addHandler(Consumer<ComponentOption<B, ?>> handler) {
        this.optionHandler.add(handler);
    }
}
