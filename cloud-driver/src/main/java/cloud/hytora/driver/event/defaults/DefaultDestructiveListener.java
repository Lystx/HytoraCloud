package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.EventOrder;
import cloud.hytora.driver.event.RegisteredListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@AllArgsConstructor
@Getter
public class DefaultDestructiveListener<E extends CloudEvent> implements DestructiveListener {

	private final Class<E> eventClass;
	private final BiConsumer<DestructiveListener, ? super E> action;
	private final Consumer<DestructiveListener> destroyHandler;

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

	@Override
	public void destroy() {
		destroyHandler.accept(this);
	}
}
