package cloud.hytora.driver.module.controller.base;

import cloud.hytora.driver.module.controller.DriverModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleConfiguration {

    String name();

    Class<? extends DriverModule> main();

    String[] author() default "No Author";

    String[] depends() default "";

    String description() default "No description";

    String version() default "1.0";

    String website() default "";

    ModuleCopyType copyType();

    ModuleEnvironment environment();

}
