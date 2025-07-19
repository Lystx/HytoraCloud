package cloud.hytora.simplejson.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter @AllArgsConstructor
public enum Primitives {

    INT(int.class, Integer.class),
    BOOLEAN(boolean.class, Boolean.class),
    CHAR(char.class, Character.class),
    LONG(long.class, Long.class),
    FLOAT(float.class, Float.class),
    SHORT(short.class, Short.class),
    BYTE(byte.class, Byte.class),
    DOUBLE(double.class, Double.class);
    ;

    /**
     * The primitive class
     */
    private final Class<?> primitiveClass;

    /**
     * The wrapper class for this primitive
     */
    private final Class<?> wrapperClass;

    /**
     * Checks if the provided class is a primitive
     *
     * @param cls the class to check
     * @return boolean
     */
    public static boolean isPrimitive(Class<?> cls) {
        return Arrays.stream(values()).anyMatch(primitive -> primitive.getPrimitiveClass().equals(cls));
    }

    /**
     * Gets a wrapper class for a class that might be a primitive
     * If it's not a primitive the provided class will be returned
     * Otherwise all values will be iterated through and the fitting
     * wrapper-class will be returned
     *
     * @param cls the class to check
     * @return the wrapper-class or self-class
     */
    public static Class<?> unwrap(Class<?> cls) {
        if (!isPrimitive(cls)) {
            return cls;
        } else {
            return Arrays.stream(values()).filter(primitive -> primitive.getPrimitiveClass().equals(cls)).findFirst().get().getWrapperClass();
        }
    }
}
