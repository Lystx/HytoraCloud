package cloud.hytora.modules.sign.spigot.manager;

import cloud.hytora.common.task.ITask;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public class BukkitCloudSignManager implements ICloudSignManager {

    /**
     * All cached cloud signs
     */
    private Collection<ICloudSign> allCachedCloudSigns;

    public BukkitCloudSignManager() {
        this.allCachedCloudSigns = new ArrayList<>();
    }

    @Override
    public void loadCloudSignsSync() {
        CloudSignAPI.getInstance().performProtocolAction(SignProtocolType.REQUEST_DATA, buffer -> {});
    }

    @Override
    public ITask<Collection<ICloudSign>> loadCloudSignsAsync() {
        loadCloudSignsSync();
        return ITask.empty(); //not needed on spigot side
    }


    @Override
    public Collection<ICloudSign> getAllCachedCloudSignsForTask(String taskName) {
        return this.allCachedCloudSigns.stream().filter(s -> s.getTaskName().equalsIgnoreCase(taskName)).collect(Collectors.toList());
    }

    @Override
    public ITask<ICloudSign> getCloudSignAsync(UUID uniqueId) {
        return ITask.callAsync(() -> this.allCachedCloudSigns.stream().filter(s -> s.getUniqueId().equals(uniqueId)).findFirst().orElse(null));
    }

    @Override
    public ICloudSign getCloudSignOrNull(UUID uniqueId) {
        return this.allCachedCloudSigns.stream().filter(s -> s.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Override
    public void addCloudSign(ICloudSign sign) {
        if (this.getCloudSignOrNull(sign.getUniqueId()) != null) {
            return;
        }
        this.allCachedCloudSigns.add(sign);

        CloudSignAPI.getInstance()
                .performProtocolAction(
                        SignProtocolType.ADD_SIGN,
                        buf -> buf.writeObject(sign)
                );
    }

    @Override
    public void removeCloudSign(ICloudSign sign) {
        ICloudSign safeSign = this.getCloudSignOrNull(sign.getUniqueId());
        if (safeSign == null) {
            return;
        }

        this.allCachedCloudSigns.remove(safeSign);

        CloudSignAPI.getInstance()
                .performProtocolAction(
                        SignProtocolType.REMOVE_SIGN,
                        buf -> buf.writeObject(safeSign)
                );
    }

    @Override
    public void update() {
        //not needed here
    }
}
