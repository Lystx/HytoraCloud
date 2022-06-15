package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.Task;

public class ValueTimedOutException extends RuntimeException {

    public ValueTimedOutException(Task<?> task) {
        super("Value has timed out!");
    }
}
