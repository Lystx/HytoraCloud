package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.Task;
import cloud.hytora.common.task.def.SimpleTask;

public class TaskTimedOutException extends RuntimeException {

    public TaskTimedOutException(Task<?> task) {
        super("Value has timed out! [" + task.hashCode());
    }
    public TaskTimedOutException() {
        super("Value has timed out!");
    }
}
