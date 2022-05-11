package cloud.hytora.common.wrapper;

public interface WrapperListener<T> {


    void onPresent(T value);

    void onStillNotPresent();
}
