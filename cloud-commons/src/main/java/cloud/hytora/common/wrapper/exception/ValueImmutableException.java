package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Task;

public class ValueImmutableException extends RuntimeException {

    public ValueImmutableException(Task<?> task) {
        super("Value holding " + (task.isPresent() ? "value of type " + task.get().getClass().getName() : "no value") + " is immutable!");
    }
}
