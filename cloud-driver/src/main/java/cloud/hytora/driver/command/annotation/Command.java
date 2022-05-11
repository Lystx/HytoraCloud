package cloud.hytora.driver.command.annotation;

import cloud.hytora.driver.command.CommandScope;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The name (and alternate names) of the command.
	 * Command names cannot contain spaces.
	 */
	@Nonnull
	String[] name();

	@Nonnull
    CommandScope scope();

	/**
	 * Only when the command can be executed ingame.
	 * Empty string for no permission required.
	 */
	@Nonnull
	String permission() default "";

}
