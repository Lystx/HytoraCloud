package cloud.hytora.driver.config;

public interface ConfigManager {

    INetworkConfig readConfig();

    IConfig readBaseConfig();

    void save();

    default IConfig universal() {
        if (getBaseConfig() == null) {
            if (getConfig() == null) {
                return null;
            } else {
                return getConfig();
            }
        } else {
            return getBaseConfig();
        }
    }

    default boolean isHeadNode() {
        return !isRemote();
    }

    boolean isRemote();

    default void save(IConfig config) {}

    INetworkConfig getConfig();

    IConfig getBaseConfig();

    void setConfig(IConfig config);
}
