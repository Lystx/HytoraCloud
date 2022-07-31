package cloud.hytora.driver.command.annotation;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {


	/**
	 * Only when the command can be executed ingame.
	 * Empty string for no permission required.
	 */
	@Nonnull
	String value() default "";

}
