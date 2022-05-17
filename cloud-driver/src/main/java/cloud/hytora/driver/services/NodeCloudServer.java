package cloud.hytora.driver.services;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;

public interface NodeCloudServer extends CloudServer {

    @Nonnull
    @CheckReturnValue
    Process getProcess();

    @Nonnull
    @CheckReturnValue
    File getWorkingDirectory();

    void setWorkingDirectory(@Nonnull File workingDirectory);

    @CheckReturnValue
    boolean isScreenServer();

    void setScreenServer(boolean value);

    void setCreationTimeStamp(long creationTime);

    void setProcess(@Nonnull Process process);
}
