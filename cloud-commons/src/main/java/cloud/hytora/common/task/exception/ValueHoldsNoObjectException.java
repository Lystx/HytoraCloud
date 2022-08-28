package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.ITask;

public class ValueHoldsNoObjectException extends RuntimeException {

    public ValueHoldsNoObjectException(ITask<?> task) {
        super("Value is not holding any object!");
    }
}
