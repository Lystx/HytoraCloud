package cloud.hytora.driver.entity.services;

import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;

/**
 * A {@link NodeSpecificCloudService} is a different {@link CloudService} that is accessible on
 * the node-side. You can access the host-provided-values like the working-directory or the {@link Process}
 *
 * @author Lystx
 * @version STABLE-1.6
 * @since STABLE-1.5
 * @see CloudService
 */
public interface NodeSpecificCloudService extends CloudService {

    /**
     * @return the channel of this service
     * @see PacketChannel
     */
    PacketChannel getChannel();

    /**
     * @return the process of this service
     */
    @Nonnull
    Process getProcess();

    /**
     * @return the directory of this service
     */
    @Nonnull
    File getWorkingDirectory();

    /**
     * Sets the dir of this service
     *
     * @param workingDirectory the dir to set
     */
    void setWorkingDirectory(@Nonnull File workingDirectory);

    /**
     * Sets the time in ms this service was created
     *
     * @param creationTime the time to set
     */
    void setCreationTimeStamp(long creationTime);

    /**
     * Sets the process instance of this service
     *
     * @param process the process to set
     */
    void setProcess(@Nonnull Process process);
}
