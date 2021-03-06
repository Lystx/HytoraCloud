package de.lystx.hytoracloud.driver.utils;

import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class CloudDriverException extends RuntimeException {

    private static final long serialVersionUID = -9111660455596144261L;

    /**
     * The extra message of this error
     */
    private final String message;

    /**
     * The code of this error
     */
    private final int code;

    /**
     * The parent of this exception
     */
    private final String parentClass;

    public CloudDriverException(Throwable throwable) {
        this.parentClass = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.code = 0x00;
    }

    public CloudDriverException(String message) {
        super(message);
        this.message = message;
        this.code = 0x00;
        this.parentClass = CloudDriverException.class.getName();
    }

    public CloudDriverException(String message, int code, Class<? extends Exception> parentClass) {
        super(message);
        this.message = message;
        this.code = code;
        this.parentClass = parentClass.getName();
    }

    @SneakyThrows
    public Class<? extends Exception> getParentClass() {
        return (Class<? extends Exception>) Class.forName(parentClass);
    }
}
