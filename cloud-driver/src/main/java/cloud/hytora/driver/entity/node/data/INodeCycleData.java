package cloud.hytora.driver.entity.node.data;

import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

/**
 * The {@link INodeCycleData} portraits a data-exchange between {@link INode} and HeadNode
 * This instance is useful to be able to retrieve statistics of this node and to check if the node has enough memory
 * to even start any more services. It is also used to check if a {@link INode} has timed out
 * based on its latest response
 *
 *
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
public interface INodeCycleData extends IBufferObject {

    /**
     * @return the time this node has started up
     */
    long getStartupTime();

    /**
     * @return the up time in milis
     */
    long getUpTime();

    /**
     * @return the cpu usage in percent
     */
    float getCpuUsage();

    /**
     * @return the cores of the machine where the node is running
     */
    int getCores();

    /**
     * @return if the node has timed out
     */
    boolean hasTimedOut();

    /**
     * @return the max RAM of the machine where the node is running
     */
    long getMaxRam();

    /**
     * @return the free RAM of the machine where the node is running
     */
    long getFreeRam();

    /**
     * @return the latency of the node
     */
    int getLatency();

    /**
     * @return the timestamp of the node
     */
    long getTimestamp();
}
