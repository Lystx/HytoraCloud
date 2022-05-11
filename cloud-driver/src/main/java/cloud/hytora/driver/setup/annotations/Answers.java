package cloud.hytora.driver.setup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Answers {

    /**
     * Answers that are not allowed
     */
    String[] forbidden() default {};

    /**
     * Answers that only may be typed
     */
    String[] only() default {};


}
