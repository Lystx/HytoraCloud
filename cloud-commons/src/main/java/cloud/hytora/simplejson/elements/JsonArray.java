
package cloud.hytora.simplejson.elements;

import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.api.enums.JsonType;
import cloud.hytora.simplejson.exceptions.JsonSerializerNotFoundException;
import lombok.Getter;

import java.util.*;
import java.util.function.BiConsumer;

@Getter
public class JsonArray extends JsonEntity implements Iterable<JsonEntity> {

    /**
     * All cached json values
     */
    private final List<JsonEntity> values;

    public JsonArray(int initialSize) {
        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            throw new JsonSerializerNotFoundException("Please instantiate a new Json instance using JsonBuilder");
        }
        this.values = new ArrayList<>(initialSize);
    }

    public JsonArray(List<JsonEntity> values) {
        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            throw new JsonSerializerNotFoundException("Please instantiate a new Json instance using JsonBuilder");
        }
        this.values = values;
    }

    /**
     * Creates a new empty array
     */
    public JsonArray() {
        this(new LinkedList<>());
    }

    public JsonArray(JsonArray array, boolean unmodifiable) {
        this(unmodifiable ? Collections.unmodifiableList(array.values) : new ArrayList<>(array.values));
    }

    /**
     * Creates a new {@link JsonArray} in one single line
     *
     * @param elements the elements to add to this array
     * @param handler the handler for the single elements
     * @param <T> the generic type
     * @return created json array
     */
    public static <T> JsonArray create(List<T> elements, BiConsumer<JsonArray, T> handler) {
        JsonArray jsonArray = new JsonArray();
        for (T element : elements) {
            handler.accept(jsonArray, element);
        }
        return jsonArray;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Adds a {@link JsonEntity} to this array
     *
     * @param value the entity
     * @return current array
     */
    public JsonArray add(JsonEntity value) {
        if (value == null) {
            value = JsonLiteral.NULL;
        }
        values.add(value);
        return this;
    }


    public JsonArray set(int index, JsonEntity value) {
        if (value == null) {
            value = JsonLiteral.NULL;
        }
        values.set(index, value);
        return this;
    }

    /**
     * Removes something under a given index
     *
     * @param index the index
     * @return current array
     */
    public JsonArray remove(int index) {
        values.remove(index);
        return this;
    }

    /**
     * The size of this array
     */
    public int size() {
        return values.size();
    }

    /**
     * Checks if this array is empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Gets a {@link JsonEntity} at a given index
     *
     * @param index the index
     * @return the entity that was found
     */
    public JsonEntity get(int index) {
        return values.get(index);
    }

    /**
     * The iterator for this array
     */
    public Iterator<JsonEntity> iterator() {
        return values.iterator();
    }

    @Override
    public JsonType jsonType() {
        return JsonType.ARRAY;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonArray asJsonArray() {
        return this;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
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
        JsonArray other = (JsonArray) object;
        return values.equals(other.values);
    }

}
