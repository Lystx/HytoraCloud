package cloud.hytora.driver.services;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;

public interface IProcessCloudServer extends ICloudServer {

    @Nonnull
    @CheckReturnValue
    Process getProcess();

    @Nonnull
    @CheckReturnValue
    File getWorkingDirectory();

    void setWorkingDirectory(@Nonnull File workingDirectory);

    void setCreationTimeStamp(long creationTime);

    void setProcess(@Nonnull Process process);
}
