package cloud.hytora.common.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaskResult<T> {

    private final TaskState state;
    private final T result;
    private final Throwable error;

    public TaskResult(TaskState state, T result) {
        this(state, result, null);
    }

    public TaskResult(TaskState state) {
        this(state, null, null);
    }
    public TaskResult(TaskState state, Throwable error) {
        this(state, null, error);
    }
}
