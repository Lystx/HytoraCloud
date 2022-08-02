package cloud.hytora.common.function;

public interface ObjectComparator<T> {

    /**
     * returns the value that is of higher priority
     */
    T compare(T t1, T t2);
}
