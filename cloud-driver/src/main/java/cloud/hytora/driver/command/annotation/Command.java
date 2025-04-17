package cloud.hytora.driver.command.annotation;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.completer.CommandCompleter;
import cloud.hytora.driver.command.completer.EmptyCompleter;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cloud.hytora.driver.command.CommandScope.CONSOLE;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The name (and alternate names) of the command.
	 * Command names cannot contain spaces.
	 */
	@Nonnull
	String[] value() default {""};

	/**
	 * The description of the command.
	 */
	@Nonnull
	String description() default "No Description set";

	/**
	 * The {@link CommandScope} of this command.
	 * From where you can execute this command
	 *
	 * @see @{@link CommandScope}
	 */
	@Nonnull
	CommandScope executionScope() default CONSOLE;

	/**
	 * Only when the command can be executed ingame.
	 * Empty string for no permission required.
	 */
	@Nonnull
	String permission() default "";

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface AutoHelp {


	}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Syntax {

        @Nonnull
        String value();

    }

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@interface Root {

	}

    /**
     * This argument defines the values for an Argument
     * that HAS TO BE shown in the {@link Syntax}.
     *
     * For example: @Argument("player") ICloudPlayer player
     * While the Syntax has to look like: @Syntax("<player>")
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Argument {

        /**
         * @return the argument's name used in the {@link Command}
         */
        @Nonnull
        String value();

        /**
         * The class of the argument completer.
         * Can only be used with {@link #words() words} {@code = 1}
         *
         * @return the class of the argument completer
         */
        @Nonnull
        Class<? extends CommandCompleter> completer() default EmptyCompleter.class;

        /**
         * The amount of words used for this argument or {@code -1} for all words left
         *
         * @return the amount of words used or {@code -1} for all remaining
         */
        int words() default 1;

        /**
         * Whether to use the raw suggestions supplied by the completer.
         * If not, they will be sorted alphabetically and filtered out when they don't start with the current input.
         *
         * @return whether to use the raw suggestions supplied by the completer
         */
        boolean raw() default false;

        /**
         * Whether this argument can be left out.
         * This can only be used if this is the last argument.
         *
         * @return whether this argument can be left out
         */
        boolean optional() default false;

    }
}
