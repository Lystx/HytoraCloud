package cloud.hytora.driver.entity.services.utils;

public enum ServiceVisibility {

    NONE,
    VISIBLE,
    INVISIBLE;

    public ServiceVisibility opposite() {
        return (this == INVISIBLE ? VISIBLE : INVISIBLE);
    }

    public String toString() {
        return this == NONE ? "§7Unknown" : (this == VISIBLE ? "§aVisible" : "§cInvisible");
    }
}
