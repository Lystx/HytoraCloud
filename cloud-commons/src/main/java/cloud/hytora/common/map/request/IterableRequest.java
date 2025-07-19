package cloud.hytora.common.map.request;

import cloud.hytora.common.map.MapEntry;
import cloud.hytora.common.map.UniqueMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterableRequest<K, V> implements Iterable<MapEntry<K, V>> {

    private final UniqueMap<K, V> parent;

    public IterableRequest(UniqueMap<K, V> parent) {
        this.parent = parent;
    }


    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.forEach(kvPair -> action.accept(kvPair.getKey(), kvPair.getValue()));
    }

    public Stream<MapEntry<K, V>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @NotNull
    @Override
    public Iterator<MapEntry<K, V>> iterator() {
        List<MapEntry<K, V>> list = new ArrayList<>();
        for (Integer integer : this.parent.getValues().keySet()) {
            list.add(this.parent.getValues().get(integer));
        }
        return list.iterator();
    }
}
