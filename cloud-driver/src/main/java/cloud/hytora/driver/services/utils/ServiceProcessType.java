package cloud.hytora.driver.services.utils;

public enum ServiceProcessType {

    /**
     * Services are being started in a wrapper process
     */
    WRAPPER,

    /**
     * No wrapper is being used
     * Only server is being started
     * and the bridge plugin replaces the wrapper
     */
    BRIDGE_PLUGIN
}
