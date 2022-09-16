package cloud.hytora.modules.sign.api;

import cloud.hytora.common.task.Task;

import java.util.Collection;
import java.util.UUID;

public interface ICloudSignManager {

    void loadCloudSignsSync();

    Task<Collection<ICloudSign>> loadCloudSignsAsync();

    void setAllCachedCloudSigns(Collection<ICloudSign> cloudSigns);

    Collection<ICloudSign> getAllCachedCloudSigns();

    Collection<ICloudSign> getAllCachedCloudSignsForTask(String taskName);

    Task<ICloudSign> getCloudSignAsync(UUID uniqueId);

    ICloudSign getCloudSignOrNull(UUID uniqueId);

    void addCloudSign(ICloudSign sign);

    void removeCloudSign(ICloudSign sign);

    void update();
}
