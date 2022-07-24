package cloud.hytora.driver.common;

/**
 * Objects that implement this interface
 * can replace placeholders within texts of their own class
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface MessagePlaceholder {

    /**
     * Applies the placeholders to the text
     *
     * @param input the input text
     */
    String replacePlaceHolders(String input);
}
