package cloud.hytora.driver.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.concurrent.TimeUnit;

public interface IServiceCrashPrevention extends IBufferObject {

    boolean isEnabled();

    void setEnabled(boolean b);

    TimeUnit getUnit();

    void setUnit(TimeUnit unit);

    long getTime();

    void setTime(long time);
}
