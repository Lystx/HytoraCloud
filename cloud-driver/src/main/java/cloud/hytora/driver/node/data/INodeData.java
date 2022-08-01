package cloud.hytora.driver.node.data;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface INodeData extends IBufferObject {

    long getStartupTime();

    long getUpTime();

    float getCpuUsage();

    int getCores();

    boolean hasTimedOut();

    long getMaxRam();

    long getFreeRam();

    int getLatency();

    long getTimestamp();
}
