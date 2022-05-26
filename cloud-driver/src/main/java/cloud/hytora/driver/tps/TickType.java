package cloud.hytora.driver.tps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum TickType {

    /**
     * All ticks within the last 60 seconds
     */
    LAST_MINUTE(60, "1 min"),

    /**
     * All ticks within the last 5 Minutes
     */
    LAST_5_MINUTES(60 * 5, "5 min"),

    /**
     * All ticks within the last 15 Minutes
     */
    LAST_15_MINUTES(60 * 15, "15 min");

    /**
     * The seconds this type takes
     */
    private final int size;

    /**
     * What is displayed
     */
    private final String label;
}
