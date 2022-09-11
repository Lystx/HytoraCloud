package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.IPromise;

public class ValueImmutableException extends RuntimeException {

    public ValueImmutableException(IPromise<?> task) {
        super("Value holding " + (task.isPresent() ? "value of type " + task.get().getClass().getName() : "no value") + " is immutable!");
    }
}
