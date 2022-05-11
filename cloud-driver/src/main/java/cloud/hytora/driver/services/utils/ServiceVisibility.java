package cloud.hytora.driver.services.utils;

public enum ServiceVisibility {

    NONE,
    VISIBLE,
    INVISIBLE;


    public String toString() {
        return this == NONE ? "§7Unknown" : (this == VISIBLE ? "§aVisible" : "§cInvisible");
    }
}
