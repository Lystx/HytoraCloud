package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.ITask;

public class ValueTimedOutException extends RuntimeException {

    public ValueTimedOutException(ITask<?> task) {
        super("Value has timed out!");
    }
}
