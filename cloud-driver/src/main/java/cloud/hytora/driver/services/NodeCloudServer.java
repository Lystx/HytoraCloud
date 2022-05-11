package cloud.hytora.driver.services;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface NodeCloudServer extends CloudServer {

    @Nonnull
    @CheckReturnValue
    Process getProcess();

    @CheckReturnValue
    boolean isScreenServer();

    void setScreenServer(boolean value);

    void setCreationTimeStamp(long creationTime);

    void setProcess(@Nonnull Process process);
}
