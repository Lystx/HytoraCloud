package cloud.hytora.modules.sign.spigot.manager;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@Getter @Setter
public class SignManager {

    /**
     * All cloudSigns (updated by packet)
     */
    private Collection<CloudSign> cloudSigns;

    /**
     * The configuration (update by packet)
     */
    private SignConfiguration configuration;

    /**
     * The sign updater
     */
    private SignUpdater signUpdater;

    public SignManager() {
        this.cloudSigns = new ArrayList<>();
        this.configuration = new SignConfiguration();
        this.signUpdater = new SignUpdater(this);
        this.run();
    }

    /**
     * Starts the Sign Scheduler
     */
    public void run() {
        try {
            new Thread(this.signUpdater, "signThread").start();
        } catch (NullPointerException e) {
            CloudDriver.getInstance().getScheduler().scheduleDelayedTask(this::run, 5L);
        }
    }
}
