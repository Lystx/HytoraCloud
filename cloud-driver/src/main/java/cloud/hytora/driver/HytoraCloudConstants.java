package cloud.hytora.driver;

public class HytoraCloudConstants {


    /**
     * The module-bridge file for communication between cloud and minecraft
     */
    public static final String BRIDGE_FILE_NAME = "cloud_bridge.jar";

    /**
     * the remote file
     *
     * @see remote-module
     */
    public static final String REMOTE_FILE_NAME = "cloud_remote.jar";


    /**
     * The interval that services take to publish their data to the cluster
     * (here: every 1.5 minutes)
     */
    public static final int SERVER_PUBLISH_INTERVAL = 90_000;

    /**
     * The max lost cycles of a server before it is declared timed out
     * (here: 3 minutes)
     */
    public static final int SERVER_MAX_LOST_CYCLES = 2;


    /**
     * The interval that nodes take to publish their data to the cluster
     * (here: every 5 seconds)
     */
    public static final int NODE_PUBLISH_INTERVAL = 5_000;

    /**
     * The max lost cycles of a node before it is declared timed out
     * (here: 25 seconds)
     */
    public static final int NODE_MAX_LOST_CYCLES = 5;

    /**
     * The public name for the Dashboard to be identified
     */
    public static final String APPLICATION_NAME = "Application";

}
