package cloud.hytora.common.wrapper.exception;


import cloud.hytora.common.wrapper.Wrapper;

public class ValueImmutableException extends RuntimeException {

    public ValueImmutableException(Wrapper<?> wrapper) {
        super("Value holding " + (wrapper.isPresent() ? "value of type " + wrapper.get().getClass().getName() : "no value") + " is immutable!");
    }
}
