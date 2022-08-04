package cloud.hytora.modules.sign.spigot;

import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.spigot.manager.BukkitCloudSignManager;
import cloud.hytora.modules.sign.spigot.manager.SignUpdater;
import lombok.Getter;

@Getter
public class BukkitCloudSignAPI extends CloudSignAPI {

    /**
     * The sign manager instance
     */
    private final ICloudSignManager signManager;

    /**
     * The sign updater instance
     */
    private final SignUpdater signUpdater;

    public BukkitCloudSignAPI() {
        super();

        this.signManager = new BukkitCloudSignManager();
        this.signUpdater = new SignUpdater();

        new Thread(this.signUpdater, "signThread").start();
    }

    @Override
    public void publishConfiguration() {

    }

}
