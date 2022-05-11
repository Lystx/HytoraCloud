package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Wrapper;

public class ValueTimedOutException extends RuntimeException {

    public ValueTimedOutException(Wrapper<?> wrapper) {
        super("Value has timed out!");
    }
}
