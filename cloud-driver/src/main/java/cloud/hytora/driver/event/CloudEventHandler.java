package cloud.hytora.driver.event;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudEventHandler {

	@Nonnull
	EventOrder order() default EventOrder.NORMAL;

	boolean ignoreCancelled() default false;

}
