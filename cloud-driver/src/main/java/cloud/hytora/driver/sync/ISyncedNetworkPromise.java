package cloud.hytora.driver.sync;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a Node-Cache-Synced Promise that contains (or doesn't) a specific value
 * that is up-to-date with the one and only cloud-cache that is outgoing from the current Node
 *
 * @param <T> the generic of the object this promise holds
 * @author Lystx
 * @since SNAPSHOT-1.5
 */
public interface ISyncedNetworkPromise<T extends IBufferObject> {

    /**
     * The object that the request has returned
     */
    @Nullable
    default T getSyncedObjectOrNull() {
        return getSyncedObject().orElse(null);
    }

    /**
     * The optional instance that might contain the requested value
     */
    @NotNull
    Optional<T> getSyncedObject();

    /**
     * The error that might have occurred
     */
    @Nullable
    Throwable getError();
}
