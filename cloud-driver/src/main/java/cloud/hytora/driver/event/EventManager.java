package cloud.hytora.driver.event;

import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.listener.DestructiveListener;
import cloud.hytora.driver.event.listener.RegisteredListener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EventManager {

	@Nonnull
	EventManager removeListener(@Nonnull RegisteredListener listener);

	@Nonnull
	EventManager addListener(@Nonnull RegisteredListener listener);

	@Nonnull
	EventManager addListeners(@Nonnull Collection<? extends RegisteredListener> listeners);

	@Nonnull
	EventManager registerListener(@Nonnull Object listener);

	@Nonnull
	EventManager unregisterListener(@Nonnull Object listener);


	@Nonnull
	<E extends LocalEvent> RegisteredListener registerHandler(@Nonnull Class<E> eventClass, @Nonnull Consumer<E> handler);

	@Nonnull
	<E extends LocalEvent> DestructiveListener registerSelfDestructiveHandler(@Nonnull Class<E> eventClass, @Nonnull Consumer<E> handler);

	<E extends LocalEvent> void registerDestructiveHandler(@Nonnull Class<E> eventClass, @Nonnull BiConsumer<E, DestructiveListener> handler);

	/**
	 * Unregisters all listeners of the given class.
	 */
	@Nonnull
	EventManager unregisterListener(@Nonnull Class<?> listenerClass);

	/**
	 * Unregisters all listeners which holder's classloader is the given classloader.
	 */
	@Nonnull
	EventManager unregisterListeners(@Nonnull ClassLoader loader);

	@Nonnull
	EventManager unregisterAll();


	@Nonnull
	<E extends LocalEvent> E callEvent(@Nonnull E event, PublishingType... type);

}
