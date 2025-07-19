package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.type.EventOrder;
import cloud.hytora.driver.event.listener.RegisteredListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;


@AllArgsConstructor
@Getter
public class DefaultRegisteredListener implements RegisteredListener {

	private final Object holder;
	private final Method method;
	private final Class<? extends LocalEvent> eventClass;
	private final EventOrder order;
	private final boolean ignoreCancelled;

	@Override
	public void execute(@Nonnull LocalEvent iEvent) throws Exception {
		method.invoke(holder, iEvent);
	}

}