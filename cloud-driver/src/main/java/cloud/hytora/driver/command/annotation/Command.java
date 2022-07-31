package cloud.hytora.driver.command.annotation;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The name (and alternate names) of the command.
	 * Command names cannot contain spaces.
	 */
	@Nonnull
	String[] value() default {""};

}
