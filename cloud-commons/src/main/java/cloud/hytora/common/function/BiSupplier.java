package cloud.hytora.common.function;

public interface BiSupplier<V, E> {

    E supply(V v);
}
