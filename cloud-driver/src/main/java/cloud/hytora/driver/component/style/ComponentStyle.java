package cloud.hytora.driver.component.style;

import cloud.hytora.driver.component.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Optional;

@Getter @AllArgsConstructor
public enum ComponentStyle {

    /**
     * The text will be bold
     */
    BOLD(0),

    /**
     * The text will be italic
     */
    ITALIC(1),

    /**
     * The text will be underlined
     */
    UNDERLINED(2),

    /**
     * The text will be striked through
     */
    STRIKETHROUGH(3),

    /**
     * A random {@link ComponentStyle} will be chosen
     *
     * (I don't know why you would use it but here you go)
     */
    RANDOM(4);

    /**
     * The id of this style
     */
    private final int id;


    /**
     * Gets a {@link ComponentStyle} by its id
     *
     * @param id the id
     * @return the style or null if not found
     */
    public static ComponentStyle fromId(@Range(from = 0, to = 4) int id) {
        return Arrays.stream(values()).filter(style -> style.getId() == id).findFirst().orElse(null);
    }

    /**
     * Gets a {@link ComponentStyle} by its id
     *
     * @param id the id
     * @return the optional style
     */
    public static Optional<ComponentStyle> fromIdOptional(@Range(from = 0, to = 4) int id) {
        return Arrays.stream(values()).filter(style -> style.getId() == id).findFirst();
    }
}
