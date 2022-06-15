package cloud.hytora.common.task;

public interface WrapperListener<T> {


    void onPresent(T value);

    void onStillNotPresent();
}
