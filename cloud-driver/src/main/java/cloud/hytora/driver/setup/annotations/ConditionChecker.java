package cloud.hytora.driver.setup.annotations;

import cloud.hytora.common.function.BiSupplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConditionChecker {

    Class<? extends BiSupplier<String, Boolean>> value();

    String message();
}
