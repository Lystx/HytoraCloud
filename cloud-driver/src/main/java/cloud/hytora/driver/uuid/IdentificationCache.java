package cloud.hytora.driver.uuid;

import cloud.hytora.common.task.ITask;

import java.util.Collection;
import java.util.UUID;

public interface IdentificationCache {

    boolean isEnabled();

    void setEnabled(boolean b);

    ITask<Collection<UUID>> loadAsync();


    void setUUID(String name, UUID uuid);

    UUID getUUID(String name);

    void update();

    Collection<UUID> getCacheLoadedUniqueIds();

}
