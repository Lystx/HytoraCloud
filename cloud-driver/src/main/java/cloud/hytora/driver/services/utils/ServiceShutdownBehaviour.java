package cloud.hytora.driver.services.utils;

public enum ServiceShutdownBehaviour {

    KEEP,

    DELETE;


    public boolean isStatic() {
        return this == KEEP;
    }

    public boolean isDynamic() {
        return this == DELETE;
    }
}
