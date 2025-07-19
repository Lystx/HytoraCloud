package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.Task;

public class TaskHoldsNoValueException extends RuntimeException {

    public TaskHoldsNoValueException(Task<?> task) {
        super("[SimpleTask] Task does not allow returning of Nulled values!");
    }
}
