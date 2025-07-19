package cloud.hytora.common.map.request;


import cloud.hytora.common.map.MapEntry;
import cloud.hytora.common.map.UniqueMap;

import java.util.Map;

public class PutRequest<K, V> {

    private final UniqueMap<K, V> parent;
    private final K key;

    private int position;

    public PutRequest(UniqueMap<K, V> parent, K key) {
        this.parent = parent;
        this.key = key;
        this.position = -1;
    }

    public PutRequest<K, V> toPosition(int pos) {
        this.position = pos;
        return this;
    }

    public void toValue(V value) {
        if (this.position == -1) {
            if (this.parent.getValues().isEmpty()) {
                this.position = 0;
            } else {
                this.position = (this.parent.getValues().keySet().size() + 1);
            }
        }
        this.position = checkRecursive(this.parent.getValues(), position);
        this.parent.getValues().put(this.position, new MapEntry<>(this.key, value, this.position));
    }

    private int checkRecursive(Map<Integer, MapEntry<K, V>> map, int index) {

        if (map.containsKey(index)) {
            //Already some value under this position stored
            index = (index + 1);
        }

        return index;
    }
}
