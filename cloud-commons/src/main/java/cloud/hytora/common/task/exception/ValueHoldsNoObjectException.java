package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.Task;

public class ValueHoldsNoObjectException extends RuntimeException {

    public ValueHoldsNoObjectException(Task<?> task) {
        super("Value is not holding any object!");
    }
}
