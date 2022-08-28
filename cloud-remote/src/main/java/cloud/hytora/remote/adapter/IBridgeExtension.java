package cloud.hytora.remote.adapter;

import cloud.hytora.driver.services.IServiceCycleData;

/**
 * This class represents an extension of the normal Remote-Functions
 * and holds the methods a CloudBridgeComponent (Bukkit, Bungee, Velocity etc.)
 * has to be able to execute by default
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
public interface IBridgeExtension {

    /**
     * Executes the given command-line from the default
     * console-command-executor of the current CloudBridgeComponent
     *
     * @param command the line to execute
     */
    void executeCommand(String command);

    /**
     * Creates a new {@link IServiceCycleData} with the
     * interesting features of the current CloudBridgeComponent
     *
     * @return cycle data instance
     */
    IServiceCycleData createCycleData();

    /**
     * Shuts down this extension
     */
    void shutdown();

    /**
     * Tries to get this extension as {@link IBridgeProxyExtension}
     *
     * @throws ClassCastException if this is called bukkit-side
     */
    IBridgeProxyExtension asProxyExtension() throws ClassCastException;
}
