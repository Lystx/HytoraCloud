package cloud.hytora.common.task.exception;


import cloud.hytora.common.task.IPromise;

public class ValueTimedOutException extends RuntimeException {

    public ValueTimedOutException(IPromise<?> task) {
        super("Value has timed out!");
    }
}
