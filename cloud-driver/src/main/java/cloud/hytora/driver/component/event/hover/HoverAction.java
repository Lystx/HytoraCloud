
package cloud.hytora.driver.component.event.hover;

/**
 * Represents an action done upon hovering over text.
 *
 * @author Lystx
 * @since 0.5-alpha
 */

public enum HoverAction {
    /**
     * Shows text, or a ChatComponent
     */
    SHOW_TEXT,

    /**
     * Shows an achievement
     */
    SHOW_ACHIEVEMENT,

    /**
     * Shows an item's metadata
     */
    SHOW_ITEM
}