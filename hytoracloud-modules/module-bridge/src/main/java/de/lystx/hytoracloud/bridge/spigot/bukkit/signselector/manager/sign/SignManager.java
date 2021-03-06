package de.lystx.hytoracloud.bridge.spigot.bukkit.signselector.manager.sign;


import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.serverselector.sign.CloudSign;
import de.lystx.hytoracloud.driver.serverselector.sign.SignConfiguration;
import de.lystx.hytoracloud.driver.service.minecraft.other.ServicePing;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter @Setter
public class SignManager {

    /**
     * All cloudSigns (updated by packet)
     */
    private List<CloudSign> cloudSigns;

    /**
     * The configuration (update by packet)
     */
    private SignConfiguration configuration;

    /**
     * The server pinger
     */
    private ServicePing servicePing;

    /**
     * The sign updater
     */
    private SignUpdater signUpdater;

    public SignManager() {
        this.cloudSigns = new LinkedList<>();
        this.configuration = SignConfiguration.createDefault();
        this.servicePing = new ServicePing();
        this.signUpdater = new SignUpdater(this);
        this.run();
    }

    /**
     * Starts the Sign Scheduler
     */
    public void run() {
        try {
            if (!CloudDriver.getInstance().getServiceManager().getThisService().getGroup().isLobby()) {
                return;
            }
            new Thread(this.signUpdater, "signThread").start();
        } catch (NullPointerException e) {
            CloudDriver.getInstance().getScheduler().scheduleDelayedTask(this::run, 5L);
        }
    }
}
