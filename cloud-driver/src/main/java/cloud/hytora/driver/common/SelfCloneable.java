package cloud.hytora.driver.common;

public interface SelfCloneable<T> {

    void cloneInternally(T from, T to);
}
