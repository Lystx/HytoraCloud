package cloud.hytora.common.map;

import cloud.hytora.common.map.request.*;

import java.util.HashMap;
import java.util.Map;

public class UniqueMap<K, V> {

    /**
     * The cached map entries and their positions
     */
    private final Map<Integer, MapEntry<K, V>> values;

    /**
     * Creates a default map with size 9999
     */
    public UniqueMap() {
        this(9999);
    }

    /**
     * Creates an empty map with a given size
     *
     * @param size the size of the map
     */
    public UniqueMap(int size) {
        this.values = new HashMap<>(size);
    }

    /**
     * Creates a {@link PutRequest} for a given key
     *
     * @param key the key to create for
     * @return request
     */
    public PutRequest<K, V> put(K key) {
        return new PutRequest<>(this, key);
    }

    /**
     * Creates {@link GetRequest}
     *
     * @return the request
     */
    public GetRequest<K, V> get() {
        return new GetRequest<>(this);
    }

    /**
     * Creates {@link RemoveRequest}
     *
     * @return the request
     */
    public RemoveRequest<K, V> remove() {
        return new RemoveRequest<>(this);
    }

    /**
     * Creates {@link IterableRequest}
     *
     * @return the request
     */
    public IterableRequest<K, V> iterable() {
        return new IterableRequest<>(this);
    }

    /**
     * Creates {@link UnsafeRequest}
     *
     * @return the request
     */
    @Deprecated
    public UnsafeRequest<K, V> unsafe() {
        return new UnsafeRequest<>(this);
    }

    /**
     * All the cache values of this map as a getter
     */
    public Map<Integer, MapEntry<K, V>> getValues() {
        return values;
    }

}
