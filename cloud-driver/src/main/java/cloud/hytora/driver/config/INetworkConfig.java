package cloud.hytora.driver.config;

/**
 * The {@link INetworkConfig} contains most of the important values for developing use.
 *
 *
 */
public interface INetworkConfig {

    /**
     * @return the port the first proxy-server is being started on
     */
    int getProxyStartPort();
    void setProxyStartPort(int port);

    /**
     * @return the port the first spigot-server is being started on
     */
    int getSpigotStartPort();
    void setSpigotStartPort(int port);

    /**
     * Updates the config and syncs it over the network
     */
    void update();
}
