package cloud.hytora.common.map.request;


import cloud.hytora.common.map.MapEntry;
import cloud.hytora.common.map.UniqueMap;

public class RemoveRequest<K, V> {

    private final UniqueMap<K, V> parent;

    public RemoveRequest(UniqueMap<K, V> parent) {
        this.parent = parent;
    }

    public void atPosition(int position) {
        this.parent.getValues().remove(position);
    }

    public void atKey(K key) {
        int position = parent.get().position(key);
        this.atPosition(position);
    }

    public void allKey(K key) {
        for (MapEntry<K, V> kvMapEntry : this.parent.iterable()) {
            if (kvMapEntry.getKey().equals(key)) {
                atKey(kvMapEntry.getKey());
            }
        }
    }

    public void allValue(V value) {
        for (MapEntry<K, V> kvMapEntry : this.parent.iterable()) {
            if (kvMapEntry.getValue().equals(value)) {
                atKey(kvMapEntry.getKey());
            }
        }
    }



    public void atValue(V value) {
        MapEntry<K, V> kvPair = this.parent.iterable().stream().filter(pair -> pair.getValue().equals(value)).findFirst().orElse(null);
        if (kvPair == null) {
            return;
        }
        K key = kvPair.getKey();
        atKey(key);
    }

}
