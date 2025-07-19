package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.type.EventOrder;
import cloud.hytora.driver.event.listener.RegisteredListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;


@AllArgsConstructor
@Getter
public class ActionRegisteredListener<E extends LocalEvent> implements RegisteredListener {

	private final Class<E> eventClass;
	private final BiConsumer<RegisteredListener, ? super E> action;

	@Override
	public void execute(@Nonnull LocalEvent iEvent) {
		action.accept(this, eventClass.cast(iEvent));
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
