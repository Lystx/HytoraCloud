package cloud.hytora.driver.node.data;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.services.ICloudServer;

/**
 * The {@link INodeCycleData} represents the last data that was
 * caught of an {@link INode} process.
 * It contains mostly statistical values like memory, startup, etc.
 *
 * @author Lystx
 * @version SNAPSHOT-1.1
 */
public interface INodeCycleData extends IBufferObject {

    /**
     * Returns the time the node was started
     */
    long getStartupTime();

    /**
     * Returns the time the node has been online for
     */
    long getUpTime();

    /**
     * Returns the cpu usage in percent of this node
     */
    float getCpuUsage();

    /**
     * Returns the cores of this node
     */
    int getCores();

    /**
     * Checks if this node has timed out (gives no response)
     */
    boolean hasTimedOut();

    /**
     * Returns the max ram in MB that this node is allowed to use
     */
    long getMaxRam();

    /**
     * Returns the RAM in MB that is still free to use
     */
    long getFreeRam();

    /**
     * Returns the Packet-Latency between Node <=> Service
     */
    int getLatency();

    /**
     * Returns the time this data has been retrieved
     */
    long getTimestamp();
}
