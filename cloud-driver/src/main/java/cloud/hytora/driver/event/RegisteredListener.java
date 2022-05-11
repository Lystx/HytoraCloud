package cloud.hytora.driver.event;

import javax.annotation.Nonnull;


public interface RegisteredListener {

	void execute(@Nonnull CloudEvent cloudEvent) throws Exception;

	@Nonnull
	Class<? extends CloudEvent> getEventClass();

	@Nonnull
	EventOrder getOrder();

	boolean isIgnoreCancelled();

	@Nonnull
	Object getHolder();

}
