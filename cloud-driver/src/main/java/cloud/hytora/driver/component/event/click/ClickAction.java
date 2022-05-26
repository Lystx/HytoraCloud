
package cloud.hytora.driver.component.event.click;

/**
 * Represents an action done upon clicking on text.
 *
 * @author Lystx
 * @since 1.0
 */
public enum ClickAction {

    /**
     * Opens a URL
     */
    OPEN_URL,

    /**
     * Opens a file
     */
    OPEN_FILE,

    /**
     * Runs a command
     */
    RUN_COMMAND,

    /**
     * Suggests a command (places text in chat-text-field)
     */
    SUGGEST_COMMAND
}