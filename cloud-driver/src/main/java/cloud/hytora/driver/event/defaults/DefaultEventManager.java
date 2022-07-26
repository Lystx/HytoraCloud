package cloud.hytora.driver.event.defaults;

import cloud.hytora.common.collection.ClassWalker;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.*;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.packets.DriverCallEventPacket;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class DefaultEventManager implements EventManager {

	private final Map<Class<? extends CloudEvent>, List<RegisteredListener>> listeners = new LinkedHashMap<>();

	@Nonnull
	@Override
	public EventManager removeListener(@Nonnull RegisteredListener listener) {
		for (Class<? extends CloudEvent> aClass : listeners.keySet()) {
			List<RegisteredListener> registeredListeners = new ArrayList<>(listeners.get(aClass));
			registeredListeners.removeIf(current -> current == listener);
			listeners.put(aClass, registeredListeners);
		}
		return this;
	}

	@Nonnull
	@Override
	public EventManager addListener(@Nonnull RegisteredListener listener) {
		return addListeners(Collections.singletonList(listener));
	}

	@Nonnull
	@Override
	public EventManager addListeners(@Nonnull Collection<? extends RegisteredListener> listeners) {
		for (RegisteredListener listener : listeners) {
			List<RegisteredListener> registeredListeners = this.listeners.computeIfAbsent(listener.getEventClass(), key -> new ArrayList<>());
			registeredListeners.add(listener);
			registeredListeners.sort(Comparator.comparingInt(value -> value.getOrder().ordinal()));
		}
		return this;
	}

	@Nonnull
	@Override
	public EventManager registerListener(@Nonnull Object listener) {
		for (Method method : ReflectionUtils.getMethodsAnnotatedWith(listener.getClass(), EventListener.class)) {

			if (method.getParameterCount() != 1 || !Modifier.isPublic(method.getModifiers())) {
				throw new IllegalArgumentException(String.format(
					"Listener method %s:%s has to be public with exactly one argument",
					listener.getClass().getName(),
					method.getName()
				));
			}

			Class<?> parameterType = method.getParameterTypes()[0];
			if (!CloudEvent.class.isAssignableFrom(parameterType)) {
				throw new IllegalArgumentException(String.format(
					"Parameter type %s of listener method %s:%s is not an event",
					parameterType.getName(),
					listener.getClass().getName(),
					method.getName()
				));
			}

			EventListener annotation = method.getAnnotation(EventListener.class);
			addListener(new DefaultRegisteredListener(listener, method, parameterType.asSubclass(CloudEvent.class), annotation.order(), annotation.ignoreCancelled()));
		}

		return this;
	}

	@Nonnull
	@Override
	public EventManager unregisterListener(@Nonnull Object holder) {
		listeners.forEach((eventClass, listeners) -> listeners.removeIf(listener -> listener.getHolder() == listener));
		return this;
	}

	@NotNull
	@Override
	public <E extends CloudEvent> RegisteredListener registerHandler(@NotNull Class<E> eventClass, @NotNull Consumer<E> handler) {
		RegisteredListener listener = new ActionRegisteredListener<>(eventClass, (registeredListener, e) -> handler.accept(e));
		this.addListener(listener);
		return listener;
	}

	@NotNull
	@Override
	public <E extends CloudEvent> DestructiveListener registerSelfDestructiveHandler(@NotNull Class<E> eventClass, @NotNull Consumer<E> handler) {
		DestructiveListener listener = new DefaultDestructiveListener<E>(eventClass, (l, e) -> {
			handler.accept(e);
			removeListener(l);
		}, this::removeListener);
		this.addListener(listener);
		return listener;
	}


	@Override
	public <E extends CloudEvent> void registerDestructiveHandler(@NotNull Class<E> eventClass, @NotNull BiConsumer<E, DestructiveListener> handler) {
		DestructiveListener listener = new DefaultDestructiveListener<E>(eventClass, (l, e) -> {
			handler.accept(e, l);
		}, this::removeListener);
		this.addListener(listener);

	}

	@Nonnull
	@Override
	public EventManager unregisterListener(@Nonnull Class<?> listenerClass) {
		for (Class<? extends CloudEvent> aClass : listeners.keySet()) {
			List<RegisteredListener> registeredListeners = new ArrayList<>(listeners.get(aClass));
			registeredListeners.removeIf(current -> current.getHolder().getClass() == listenerClass);
			listeners.put(aClass, registeredListeners);
		}
		return this;
	}

	@Nonnull
	@Override
	public EventManager unregisterListeners(@Nonnull ClassLoader loader) {
		for (Class<? extends CloudEvent> aClass : listeners.keySet()) {
			List<RegisteredListener> registeredListeners = new CopyOnWriteArrayList<>(listeners.get(aClass));
			registeredListeners.removeIf(listener -> listener != null && listener.getHolder() != null && loader != null && listener.getHolder().getClass().getClassLoader().equals(loader));
			listeners.put(aClass, registeredListeners);
		}
		return this;
	}

	@Nonnull
	@Override
	public EventManager unregisterAll() {
		listeners.clear();
		return this;
	}

	@Nonnull
	@Override
	public <E extends CloudEvent> E callEventGlobally(@Nonnull E event) {
		this.callEventOnlyLocally(event); //calling locally
		if (event instanceof ProtocolTansferableEvent) {
			ProtocolTansferableEvent pEvent = (ProtocolTansferableEvent)event;
			this.callEventOnlyPacketBased(pEvent); //calling packet if protocol event
		}
		return event;
	}

	@NotNull
	@Override
	public <E extends ProtocolTansferableEvent> E callEventOnlyPacketBased(@NotNull E event) {
		AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
		executor.sendPacket(new DriverCallEventPacket(event));
		return event;
	}

	@NotNull
	@Override
	public <E extends CloudEvent> E callEventOnlyLocally(@NotNull E event) {
		if (event == null) {
			return null;
		}
		if (!(event instanceof DriverLogEvent)) {
			CloudDriver.getInstance().getLogger().trace("Calling event {} | {}", event.getClass().getSimpleName(), event);
		}

		for (Class<?> clazz : ClassWalker.walk(event.getClass())) {
			List<RegisteredListener> listeners = this.listeners.get(clazz);
			if (listeners == null) {
				continue;
			}
			for (RegisteredListener listener : new ArrayList<>(listeners)) {
				if (listener == null) {
					continue;
				}
				if (listener.isIgnoreCancelled() && event instanceof Cancelable && ((Cancelable)event).isCancelled()) continue;

				try {
					listener.execute(event);
				} catch (Throwable ex) {
					CloudDriver.getInstance().getLogger().error("An error uncaught occurred while executing event listener", ex);
				}
			}
		}

		if (event instanceof Cancelable) {
			CloudDriver.getInstance().getLogger().trace("=> {}: cancelled={}", event.getClass().getSimpleName(), ((Cancelable) event).isCancelled());
		}
		return event;
	}

	@Nonnull
	@Override
	public <E extends CloudEvent> Task<E> nextEvent(@Nonnull Class<E> eventClass) {
		Task<E> task = Task.empty();
		RegisteredListener listener = new ActionRegisteredListener<>(eventClass, ((registeredListener, e) -> task.setResult(e)));
		addListener(listener);
		task.addUpdateListener(wrapper -> {
			if (wrapper.isSuccess()) {
				removeListener(listener);
			}
		});
		return task;
	}

}
