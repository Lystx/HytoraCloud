package cloud.hytora.common.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LogLevel {

    TRACE(0, false),
    DEBUG(2, false),
    STATUS(7, false),
    INFO(10, false),
    WARN(15, true),
    ERROR(25, true);

    /**
     * The value of this level
     */
    private final int value;

    /**
     * If this level is highlighted
     */
    private final boolean highlighted;

    public boolean isEnabled(@Nonnull LogLevel loggerLevel) {
        return this.getValue() >= loggerLevel.getValue();
    }

    @Nonnull
    public String getName() {
        return name().toUpperCase();
    }

    @Nonnull
    public static LogLevel fromName(@Nonnull String name) {
        return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name)).findFirst().orElse(INFO);
    }
}
