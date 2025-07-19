package cloud.hytora.driver.event.listener;

import cloud.hytora.driver.event.type.EventOrder;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {

	@Nonnull
    EventOrder order() default EventOrder.NORMAL;

	boolean ignoreCancelled() default false;

}
