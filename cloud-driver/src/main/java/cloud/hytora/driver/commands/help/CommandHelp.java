package cloud.hytora.driver.commands.help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IMPORTANT: Argument help starts with counting at '0'<br><br>
 * Syntax for the method:<br>
 * {@literal @}ArgumentHelp
 * public void methodName({@link CommandHelper} helper){}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHelp {

}
