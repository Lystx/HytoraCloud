package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Task;

public class ValueTimedOutException extends RuntimeException {

    public ValueTimedOutException(Task<?> task) {
        super("Value has timed out!");
    }
}
