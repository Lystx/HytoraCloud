package cloud.hytora.driver.event.listener;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.type.EventOrder;

import javax.annotation.Nonnull;


public interface RegisteredListener {

	void execute(@Nonnull LocalEvent iEvent) throws Exception;

	@Nonnull
	Class<? extends LocalEvent> getEventClass();

	@Nonnull
    EventOrder getOrder();

	boolean isIgnoreCancelled();

	@Nonnull
	Object getHolder();

}
