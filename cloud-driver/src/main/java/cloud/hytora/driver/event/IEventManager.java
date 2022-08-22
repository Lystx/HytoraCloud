package cloud.hytora.driver.event;

import cloud.hytora.common.task.Task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IEventManager {

	@Nonnull
    IEventManager removeListener(@Nonnull RegisteredListener listener);

	@Nonnull
    IEventManager addListener(@Nonnull RegisteredListener listener);

	@Nonnull
    IEventManager addListeners(@Nonnull Collection<? extends RegisteredListener> listeners);

	@Nonnull
    IEventManager registerListener(@Nonnull Object listener);

	@Nonnull
    IEventManager unregisterListener(@Nonnull Object listener);


	@Nonnull
	<E extends CloudEvent> RegisteredListener registerHandler(@Nonnull Class<E> eventClass, @Nonnull Consumer<E> handler);

	@Nonnull
	<E extends CloudEvent> DestructiveListener registerSelfDestructiveHandler(@Nonnull Class<E> eventClass, @Nonnull Consumer<E> handler);

	<E extends CloudEvent> void registerDestructiveHandler(@Nonnull Class<E> eventClass, @Nonnull BiConsumer<E, DestructiveListener> handler);

	/**
	 * Unregisters all listeners of the given class.
	 */
	@Nonnull
    IEventManager unregisterListener(@Nonnull Class<?> listenerClass);

	/**
	 * Unregisters all listeners which holder's classloader is the given classloader.
	 */
	@Nonnull
    IEventManager unregisterListeners(@Nonnull ClassLoader loader);

	@Nonnull
    IEventManager unregisterAll();

	@Nonnull
	<E extends CloudEvent> E callEventGlobally(@Nonnull E event);

	@Nonnull
	<E extends ProtocolTansferableEvent> E callEventOnlyPacketBased(@Nonnull E event);

	@Nonnull
	<E extends CloudEvent> E callEventOnlyLocally(@Nonnull E event);

	@Nonnull
	<E extends CloudEvent> Task<E> nextEvent(@Nonnull Class<E> eventClass);

	@Nonnull
	default <E extends CloudEvent> E awaitNextEvent(@Nonnull Class<E> eventClass) {
		return nextEvent(eventClass).syncUninterruptedly().get();
	}

	@Nullable
	default <E extends CloudEvent> E awaitNextEvent(@Nonnull Class<E> eventClass, long timeout, @Nonnull TimeUnit unit) {
		return nextEvent(eventClass).timeOut(unit, (int) timeout).syncUninterruptedly().get();
	}

}
