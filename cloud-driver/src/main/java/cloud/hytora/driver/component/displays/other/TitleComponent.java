
package cloud.hytora.driver.component.displays.other;

import cloud.hytora.driver.component.ChatComponent;

/**
 * Represents a Title which is displayed in the
 * center of the client's screen. A title consists
 * of a main title and a subtitle, both of which are
 * optional.
 *
 * <p>Titles are independent of the player they are sent to.
 * for every change after it is already sent in order for
 * the new changes to be effected.</p>
 *
 * <p>Unlike most classes in the VeraProxy API, this class
 * is <strong>not thread-safe</strong>.<p>
 *
 * @author Lystx
 * @since 1.0
 */
public interface TitleComponent {

    /**
     * The default number of ticks that it takes the title
     * to become the full opaqueness
     */
    int DEFAULT_FADE_IN = 10;

    /**
     * The default number of ticks that the title will stay
     * on the client's screen
     */
    int DEFAULT_STAY = 70;

    /**
     * The default number of ticks that it takes for the
     * title to become fully transparent and disappear from
     * the player's screen
     */
    int DEFAULT_FADE_OUT = 20;

    /**
     * Gets the main title.
     *
     * @return The main title.
     */
    ChatComponent getHeader();

    /**
     * Sets the main title.
     *
     * @param value The new title.
     * @return This title.
     */
    TitleComponent setHeader(ChatComponent value);

    /**
     * Gets the subtitle.
     *
     * @return The subtitle.
     */
    ChatComponent getFooter();

    /**
     * Sets the subtitle.
     *
     * @param value The new subtitle.
     * @return This title.
     */
    TitleComponent setFooter(ChatComponent value);

    /**
     * Gets the fade in timing for the title
     * and subtitle.
     *
     * @return The fade in timing.
     */
    int getFadeIn();

    /**
     * Sets the fade in timing for the title
     * and subtitle.
     *
     * @param fadeIn Number of ticks to fade in.
     * @return This title.
     */
    TitleComponent setFadeIn(int fadeIn);

    /**
     * Gets the stay timing for the title
     * and subtitle.
     *
     * @return The stay timing.
     */
    int getStay();

    /**
     * Sets the stay timing for the title
     * and subtitle.
     *
     * @param stay Number of ticks to stay on screen.
     * @return This title.
     */
    TitleComponent setStay(int stay);

    /**
     * Gets the fade out timing for the title
     * and subtitle.
     *
     * @return The fade out timing in ticks.
     */
    int getFadeOut();

    /**
     * Sets the fade out timing for the title
     * and subtitle.
     *
     * @param fadeOut Number of ticks to fade out.
     * @return This title.
     */
    TitleComponent setFadeOut(int fadeOut);

}