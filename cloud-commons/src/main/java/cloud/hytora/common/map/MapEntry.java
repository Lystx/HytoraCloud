package cloud.hytora.common.map;

public class MapEntry<K, V> {

    private final K key;
    private final V value;

    private final int position;

    public MapEntry(K key, V value, int position) {
        this.key = key;
        this.value = value;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "[Pos: " + position + "] " + key + " = " + value;
    }
}
