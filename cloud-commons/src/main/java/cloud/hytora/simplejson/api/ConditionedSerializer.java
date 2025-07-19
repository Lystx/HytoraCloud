package cloud.hytora.simplejson.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public class ConditionedSerializer<T> {

    private final JsonSerializer<T> serializer;
    private final Predicate<Class<?>> condition;
}
