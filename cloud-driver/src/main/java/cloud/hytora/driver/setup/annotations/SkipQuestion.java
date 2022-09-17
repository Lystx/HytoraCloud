package cloud.hytora.driver.setup.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(SkipContainer.class)
public @interface SkipQuestion {

    /**
     * The id of the question that should be skipped
     */
    int id();

    /**
     * The id of the question that should be checked if needs to be skipped
     */
    int checkId();

    /**
     * If any of these values has been set then the question will be set
     */
    String[] values();
}
