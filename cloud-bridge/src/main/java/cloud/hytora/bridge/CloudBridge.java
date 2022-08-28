package cloud.hytora.bridge;

import cloud.hytora.bridge.proxy.UniversalProxyPlayerExecutorHandler;
import cloud.hytora.bridge.universal.UniversalBridgeCommandHandler;
import cloud.hytora.bridge.universal.UniversalBridgeUpdateHandler;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.IBridgeExtension;
import lombok.Getter;
import lombok.Setter;

import static cloud.hytora.driver.CloudDriver.SERVER_PUBLISH_INTERVAL;

public class CloudBridge {

    /**
     * The remote extension for bridge instance
     * @see IBridgeExtension
     */
    @Setter @Getter
    private static IBridgeExtension remoteExtension;

    /**
     * The bridge plugin implementation
     * @see IBridgePlugin
     */
    @Setter @Getter
    private static IBridgePlugin plugin;

    /**
     * Init the whole bridge and loads different universal
     * methods to not double implement those methods
     */
    public static void init() {

        //only needed on proxy side
        if (Remote.getInstance().getProperty().getVersionType().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            UniversalProxyPlayerExecutorHandler.init();
        }

        //registering handler
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new UniversalBridgeUpdateHandler());
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new UniversalBridgeCommandHandler());
    }

    /**
     * Updates the current server info of
     * the server this bridge is running on
     */
    public static void updateServiceInfo() {
        ICloudServer server = Remote.getInstance().thisSidesClusterParticipant();

        if (remoteExtension == null || server == null) {
            return;
        }
        server.setLastCycleData(remoteExtension.createCycleData());
        server.update();
    }

    /**
     * Starts the service updating
     * task that updates the own server info every interval
     * that is defined in {@link CloudDriver}
     */
    public static void startUpdateTask() {
        //service cycle updateTask task
        Scheduler
                .runTimeScheduler()
                .scheduleRepeatingTaskAsync(
                        CloudBridge::updateServiceInfo,
                        SERVER_PUBLISH_INTERVAL,
                        SERVER_PUBLISH_INTERVAL
                );
    }
}
