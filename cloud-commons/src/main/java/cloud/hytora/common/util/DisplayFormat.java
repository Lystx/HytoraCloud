package cloud.hytora.common.util;

import lombok.Getter;

/**
 * This format is for sending complex message formats to a user simply (like lists, ..)
 * To create one, just extend the DisplayFormat.
 */
public abstract class DisplayFormat {

    @Getter
    private DoubleList<String, Boolean> components = new DoubleList<>();

    /**
     * In this method you can add components
     */
    public abstract void setupComponents();

    /**
     * Prepares this shit?
     *
     * @return The result
     */
    public boolean prepare() {
        setupComponents();
        return components != null;
    }

    /**
     * Adds a component to the components list to be sent to the player
     *
     * @param message   The component to be added
     * @param condition The condition
     * @return This
     */
    protected DisplayFormat addMessage(String message, boolean condition) {
        components.add(message, condition);
        return this;
    }

    protected DisplayFormat addMessage(String message) {
        return addMessage(message, true);
    }

    /**
     * Gets the message of this key or the key itself (if it's not a key lUl)
     *
     * @param s The key or message
     * @return The message
     */
    protected String getMessage(String s, Object... replacements) {
        if(s.isEmpty()) return "";
        return s;
    }

}
