package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.IPromise;

public class ValueHoldsNoObjectException extends RuntimeException {

    public ValueHoldsNoObjectException(IPromise<?> task) {
        super("Value is not holding any object!");
    }
}
