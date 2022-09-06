package cloud.hytora.remote.impl;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.sync.ISyncedNetworkPromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class RemoteSyncedNetworkPromise<T extends IBufferObject> implements ISyncedNetworkPromise<T> {

    private Throwable error;
    private T object;

    @Override
    public @NotNull Optional<T> getSyncedObject() {
        return Optional.ofNullable(object);
    }

}
