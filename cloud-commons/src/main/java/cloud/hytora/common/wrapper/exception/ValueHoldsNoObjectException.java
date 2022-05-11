package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Wrapper;

public class ValueHoldsNoObjectException extends RuntimeException {

    public ValueHoldsNoObjectException(Wrapper<?> wrapper) {
        super("Value is not holding any object!");
    }
}
