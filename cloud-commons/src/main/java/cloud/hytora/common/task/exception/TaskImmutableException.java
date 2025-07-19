package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.Task;

public class TaskImmutableException extends RuntimeException {

    public TaskImmutableException(Task<?> task) {
        super("Value holding " + (task.isPresent() ? "value of type " + task.get().getClass().getName() : "no value") + " is immutable!");
    }
}
