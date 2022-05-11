package cloud.hytora.driver.setup.annotations;

import cloud.hytora.driver.setup.SetupSuggester;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AnswerCompleter {

    Class<? extends SetupSuggester> value();
}
