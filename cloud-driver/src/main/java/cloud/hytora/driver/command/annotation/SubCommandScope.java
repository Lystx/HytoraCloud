package cloud.hytora.driver.command.annotation;

import cloud.hytora.driver.command.CommandScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandScope {


    CommandScope value();
}
