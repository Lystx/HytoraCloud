
package cloud.hytora.simplejson.elements;


import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.api.enums.JsonType;
import cloud.hytora.simplejson.exceptions.JsonSerializerNotFoundException;

public class JsonLiteral extends JsonEntity {

    /**
     * The null instance
     */
    public static final JsonEntity NULL = new JsonLiteral(Type.NULL);

    /**
     * The true instance ({@link Boolean)}
     */
    public static final JsonEntity TRUE = new JsonLiteral(Type.TRUE);

    /**
     * The false instance ({@link Boolean)}
     */
    public static final JsonEntity FALSE = new JsonLiteral(Type.FALSE);

    /**
     * The enum type
     */
    private final Type value;

    private JsonLiteral(Type value) {
        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            throw new JsonSerializerNotFoundException("Please instantiate a new Json instance using JsonBuilder");
        }
        this.value = value;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public JsonType jsonType() {
        return this.value == Type.NULL ? JsonType.NULL : JsonType.BOOLEAN;
    }

    @Override
    public boolean isNull() {
        return value == Type.NULL;
    }

    @Override
    public boolean isTrue() {
        return value == Type.TRUE;
    }

    @Override
    public boolean isFalse() {
        return value == Type.FALSE;
    }

    @Override
    public boolean isBoolean() {
        return value != Type.NULL;
    }

    @Override
    public Boolean asBoolean() {
        return value == Type.NULL ? super.asBoolean() : value == Type.TRUE;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        JsonLiteral other = (JsonLiteral) object;
        return value == other.value;
    }

    @Override
    public String toString() {
        switch (value) {
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NULL:
                return "null";
            default:
                return null;
        }
    }


    private enum Type {

        TRUE,
        FALSE,
        NULL
    }
}
