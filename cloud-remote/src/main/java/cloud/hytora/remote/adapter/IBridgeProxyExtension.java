package cloud.hytora.remote.adapter;

import cloud.hytora.driver.services.ICloudServer;

/**
 * This class represents an extension of the {@link IBridgeExtension}
 * and extends the Remote-Functions af a Proxy-Server
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
public interface IBridgeProxyExtension extends IBridgeExtension {

    /**
     * Puts a new {@link ICloudServer} into the cache of
     * the current proxy-software instance and saves it
     *
     * @param server the server to register
     */
    void registerService(ICloudServer server);

    /**
     * Removes the provided {@link ICloudServer} from the cache
     * of the current proxy-software instance
     *
     * @param server the server to unregister
     */
    void unregisterService(ICloudServer server);

    /**
     * Clears all (pre) registered service infos
     */
    void clearServices();

    /**
     * Registers all network handlers for
     * player execution actions
     */
    void registerExecutorHandlers();

}
