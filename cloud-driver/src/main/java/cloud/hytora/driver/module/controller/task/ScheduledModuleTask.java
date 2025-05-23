
package cloud.hytora.driver.module.controller.task;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScheduledModuleTask {

    /**
     * If this should be executed sync
     *
     * @return boolean
     */
    boolean sync() default true;

    /**
     * The delay of this scheduled task
     *
     * @return long delay
     */
    long delay() default  -1;

    /**
     * The repeating tick if it should repeat
     *
     * @return long tick
     */
    long repeat() default -1;

}
