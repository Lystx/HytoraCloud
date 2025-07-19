
package cloud.hytora.simplejson.elements;


import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.api.enums.JsonType;
import cloud.hytora.simplejson.exceptions.JsonSerializerNotFoundException;

import java.util.UUID;

public class JsonString extends JsonEntity {

    /**
     * The string instance
     */
    private final String string;

    public JsonString(String string) {
        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            throw new JsonSerializerNotFoundException("Please instantiate a new Json instance using JsonBuilder");
        }
        if (string == null) {
            throw new NullPointerException("JsonString: Can't set nulled String as value!");
        }
        this.string = string;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public JsonType jsonType() {
        return JsonType.STRING;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public String asString() {
        return string;
    }

    @Override
    public UUID asUUID() {
        return UUID.fromString(string);
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
        JsonString other = (JsonString) object;
        return string.equals(other.string);
    }
}
