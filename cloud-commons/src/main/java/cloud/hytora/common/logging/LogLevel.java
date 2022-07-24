package cloud.hytora.common.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LogLevel {

    NULL(-1, false),
    TRACE(0, ConsoleColor.YELLOW),
    DEBUG(2, ConsoleColor.YELLOW),
    STATUS(7, false),
    INFO(10, false),
    WARN(15, ConsoleColor.ORANGE),
    ERROR(25,  ConsoleColor.RED);

    /**
     * The value of this level
     */
    private final int value;

    /**
     * If this level is highlighted
     */
    private final boolean highlighted;

    /**
     * The custom highlight color
     */
    private final ConsoleColor highlightColor;

    LogLevel(int value, ConsoleColor highlightColor) {
        this(value, true, highlightColor);
    }

    LogLevel(int value, boolean highlighted) {
        this.value = value;
        this.highlighted = highlighted;
        this.highlightColor =  null;
    }

    public boolean isEnabled(@Nonnull LogLevel loggerLevel) {
        if (this == NULL) return true;
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
