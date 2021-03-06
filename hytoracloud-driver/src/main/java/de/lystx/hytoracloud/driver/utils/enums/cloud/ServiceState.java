package de.lystx.hytoracloud.driver.utils.enums.cloud;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter @AllArgsConstructor
public enum ServiceState implements Serializable {

    /**
     * This service is full
     */
    FULL("§6", 1),

    /**
     * The group of it is in maintenance
     */
    MAINTENANCE("§b", 3),

    /**
     * The service is starting at the moment
     */
    BOOTING("§e", 4),

    /**
     * The service is offline
     */
    OFFLINE("§4", 14),

    /**
     * The service is ingame
     */
    INGAME("§c", 14),

    /**
     * The service is in lobby phase
     */
    AVAILABLE("§a", 5);

    private final String color;

    private final int blockId;
}
