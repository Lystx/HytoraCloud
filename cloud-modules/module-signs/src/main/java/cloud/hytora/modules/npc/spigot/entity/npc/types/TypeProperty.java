package cloud.hytora.modules.npc.spigot.entity.npc.types;

import java.util.function.Function;

public enum TypeProperty {
    STRING(String::toString),
    BOOLEAN(Boolean::parseBoolean),
    INT(Integer::parseInt),
    DOUBLE(Double::parseDouble),
    FLOAT(Float::parseFloat),
    SHORT(Short::parseShort),
    LONG(Long::parseLong);

    private final Function<String, ?> function;

    TypeProperty(Function<String, ?> function) {
        this.function = function;
    }

    public Function<String, ?> getFunction() {
        return this.function;
    }

    public static TypeProperty forType(Class<?> primitiveType) {
        if (primitiveType == String.class)
            return STRING;
        if (primitiveType == boolean.class)
            return BOOLEAN;
        if (primitiveType == int.class)
            return INT;
        if (primitiveType == double.class)
            return DOUBLE;
        if (primitiveType == float.class)
            return FLOAT;
        if (primitiveType == short.class)
            return SHORT;
        if (primitiveType == long.class)
            return LONG;
        return null;
    }
}
