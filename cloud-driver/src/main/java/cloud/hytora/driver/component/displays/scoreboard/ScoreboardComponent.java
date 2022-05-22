
package cloud.hytora.driver.component.displays.scoreboard;

import java.util.List;

public interface ScoreboardComponent {

    /**
     * Sets the title of this scoreboard
     *
     * @param title the title
     */
    void setTitle(String title);

    /**
     * The title of this scoreboard
     */
    String getTitle();

    /**
     * Sets the name of the scoreboard
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * The name of the scoreboard
     */
    String getName();

    /**
     * Add a module to the scoreboard with a specific priority
     *
     * @param item The item to add
     */
    void addScore(ScoreboardEntry item);

    /**
     * All items this scoreboard has
     */
    List<ScoreboardEntry> getItems();

    /**
     * Destroys this scoreboard
     */
    void destroy();
}
