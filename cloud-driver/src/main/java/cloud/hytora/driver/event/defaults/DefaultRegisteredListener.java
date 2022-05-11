package cloud.hytora.driver.event.defaults;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.EventOrder;
import cloud.hytora.driver.event.RegisteredListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;


@AllArgsConstructor
@Getter
public class DefaultRegisteredListener implements RegisteredListener {

	private final Object holder;
	private final Method method;
	private final Class<? extends CloudEvent> eventClass;
	private final EventOrder order;
	private final boolean ignoreCancelled;

	@Override
	public void execute(@Nonnull CloudEvent cloudEvent) throws Exception {
		method.invoke(holder, cloudEvent);
	}

}