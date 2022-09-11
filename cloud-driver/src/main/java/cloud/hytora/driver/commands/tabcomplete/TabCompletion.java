package cloud.hytora.driver.commands.tabcomplete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method which is used to tab completes a command<br>
 * The parameter of this method needs to be {@link TabCompleter}<br>
 * IMPORTANT: Tab completion starts with counting at '1' and it doesn't reset index on sub commands
 * so first argument of sub command would be '2'<br>
 * Syntax for the method:<br>
 * <br>
 * {@literal @}TabCompletion<br>
 * public void methodName({@link TabCompleter} completer) {<br>
 *
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TabCompletion {

}
