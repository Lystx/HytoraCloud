package cloud.hytora.common.util;

import cloud.hytora.common.collection.pair.Pair;
import cloud.hytora.common.collection.pair.Tuple;

import java.util.ArrayList;

public class DoubleList<K, V> extends ArrayList<Tuple<K, V>> {

    /**
     * Adds a pair with given key and value to the list
     *
     * @param key   The key
     * @param value The value
     */
    public void add(K key, V value) {
        super.add(new Tuple<>(key, value));
    }

}
