package cloud.hytora.driver.event;

public interface DestructiveListener extends RegisteredListener {

    void destroy();
}
