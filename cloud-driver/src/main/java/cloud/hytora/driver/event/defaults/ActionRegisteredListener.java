package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.EventOrder;
import cloud.hytora.driver.event.RegisteredListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@AllArgsConstructor
@Getter
public class ActionRegisteredListener<E extends CloudEvent> implements RegisteredListener {

	private final Class<E> eventClass;
	private final BiConsumer<RegisteredListener, ? super E> action;

	@Override
	public void execute(@Nonnull CloudEvent cloudEvent) {
		action.accept(this, eventClass.cast(cloudEvent));
	}

	@Nonnull
	@Override
	public EventOrder getOrder() {
		return EventOrder.NORMAL;
	}

	@Override
	public boolean isIgnoreCancelled() {
		return false;
	}

	@Nonnull
	@Override
	public Object getHolder() {
		return this;
	}

}
