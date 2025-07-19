package cloud.hytora.simplejson.api.annotation;


import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.api.ExcludeStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.LOCAL_VARIABLE})
public @interface JsonSerializedObject {

    /**
     * The override custom {@link JsonFormat} to use when
     * serializing an object that is annotated with this annotation
     */
    JsonFormat customFormat() default JsonFormat.UNKNOWN;

    /**
     * The amount of times a field of an object
     * will be serialized if its the same type as the class
     * (to prevent StackOverFlow)
     */
    int serializeSameFieldInstance() default -1;

    /**
     * The fields that will be ignored
     * if the field type matches any of the classe´s
     */
    Class<?>[] excludeClasses() default {};

    /**
     * A custom {@link ExcludeStrategy} for this object
     * Warning!
     * It will clear all other strategies and only uses
     * this provided {@link ExcludeStrategy}
     */
    Class<? extends ExcludeStrategy> strategy() default ExcludeStrategy.class;

    /**
     * Overrides the option of the {@link cloud.hytora.simplejson.helper.json.Json} instance
     * if arrays should be written in one line if possible
     *
     * Only works if the field this annotation is on is really an array or list
     */
    ConsiderArrayType writeArraysSingleLined() default ConsiderArrayType.IGNORE;

    enum ConsiderArrayType {

        /**
         * This value will be ignored
         * The depending JsonSetting will be used
         */
        IGNORE,

        /**
         * It overrides the json setting
         * and writes arrays single-lined
         */
        OVERRIDE_TRUE,

        /**
         * It overrides the json setting
         * and doesn't writes arrays single-lined
         */
        OVERRIDE_FALSE,

    }
}
