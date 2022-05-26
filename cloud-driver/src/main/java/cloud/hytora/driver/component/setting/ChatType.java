
package cloud.hytora.driver.component.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the different types of messages sent to a client.
 *
 * @author Lystx
 * @since 1.0
 */
@Getter @AllArgsConstructor
public enum ChatType {

    /**
     * A chat message (such as notifications or player chat)
     */
    CHAT(0),

    /**
     * A system message (such as command output)
     */
    SYSTEM(1),

    /**
     * A message above the hot bar
     */
    ACTION_BAR(2);

    /**
     * The packet id
     */
    private final int id;

    public static ChatType getById(int id) {
        return Arrays.stream(values()).filter(v -> v.id == id).findFirst().orElse(CHAT);
    }
}
