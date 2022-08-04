package cloud.hytora.common.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
public @interface Inject {

    /**
     * The name of the field for binding with names
     */
    String value() default "";

    /**
     * If the global registered value should
     * be used if nothing is found for given name
     */
    boolean fallback() default false;
}
