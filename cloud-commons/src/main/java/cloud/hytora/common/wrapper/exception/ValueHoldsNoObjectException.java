package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Task;

public class ValueHoldsNoObjectException extends RuntimeException {

    public ValueHoldsNoObjectException(Task<?> task) {
        super("Value is not holding any object!");
    }
}
