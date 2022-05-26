
package cloud.hytora.driver.component.setting;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Chat modes which a player may set by navigating to the
 * "Multiplayer settings" and toggling the chat modes to
 * the states listed below.
 *
 * @author Lystx
 * @since 1.0
 */
public enum ClientChatMode {

    /**
     * Allows both chat messages and commands to be
     * displayed on the chat bar.
     */
    CHAT_AND_COMMANDS(0),

    /**
     * Allows only command feedback to be displayed on the
     * chat bar.
     */
    COMMANDS_ONLY(1),

    /**
     * Do not use the chat bar.
     */
    NONE(2);

    /**
     * The data as int
     */
    @Getter
    private final int data;

    /**
     * Creates a new chat mode with the given data value.
     *
     * @param data the identifier used in the protocol to
     * signify a particular chat mode
     */
    ClientChatMode(int data) {
        this.data = data;
    }

    /**
     * Obtains the chat mode of the client given the data
     * that was sent using the client settings packet.
     *
     * @param data the chat mode number
     * @return the chat mode
     */
    public static ClientChatMode of(int data) {
        try {
            return Arrays.stream(values()).filter(clientChatMode -> clientChatMode.getData() == data).findFirst().orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("no client chat mode with id=" + data));
        } catch (Throwable e) {
            return null;
        }
    }
}