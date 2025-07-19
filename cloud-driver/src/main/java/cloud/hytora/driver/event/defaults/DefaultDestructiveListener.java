package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.listener.DestructiveListener;
import cloud.hytora.driver.event.type.EventOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@AllArgsConstructor
@Getter
public class DefaultDestructiveListener<E extends LocalEvent> implements DestructiveListener {

	private final Class<E> eventClass;
	private final BiConsumer<DestructiveListener, ? super E> action;
	private final Consumer<DestructiveListener> destroyHandler;

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

	@Override
	public void destroy() {
		destroyHandler.accept(this);
	}
}
