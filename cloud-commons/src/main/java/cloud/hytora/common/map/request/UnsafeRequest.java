package cloud.hytora.common.map.request;

import cloud.hytora.document.Document;
import com.google.gson.JsonObject;

import cloud.hytora.common.map.MapEntry;
import cloud.hytora.common.map.UniqueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class UnsafeRequest<K, V> {

    /**
     * The parent map of this request
     */
    private final UniqueMap<K, V> parent;

    /**
     * Constructs this request
     *
     * @param parent the parent
     */
    public UnsafeRequest(UniqueMap<K, V> parent) {
        this.parent = parent;
    }

    /**
     * Creates a standard {@link Map} with no doubled values
     * And no filter so every value will be accepted
     *
     * @return created map
     */
    public Map<K, V> toNotUniqueHashMap() {
        return toNotUniqueHashMap(null);
    }

    /**
     * Creates a standard {@link Map} where no doubeld values
     * are allowed and only the provided filter decides which values are
     * getting accepted to be put into the map
     *
     * @param filter the filter
     * @return created map
     */
    public Map<K, V> toNotUniqueHashMap(Predicate<V> filter) {
        Map<K, V> map = new HashMap<>();
        this.parent.iterable().forEach((k, v) -> {
            if (filter == null || filter.test(v)) {
                map.put(k, v);
            }
        });
        return map;
    }

    /**
     * Transforms the parent {@link UniqueMap} into a {@link JsonObject}
     * And pays attention to doubled values
     * Json entries are sorted by their position and followed by the {@link MapEntry} as Sub- {@link JsonObject}
     *
     * @return json object
     */
    public Document toDocument() {
        Document document = Document.gson();

        for (MapEntry<K, V> entry : this.parent.iterable()) {
            document.set(String.valueOf(entry.getPosition()), Document.gson().set(String.valueOf(entry.getKey()), entry.getValue()));
        }

        return document;
    }

}
