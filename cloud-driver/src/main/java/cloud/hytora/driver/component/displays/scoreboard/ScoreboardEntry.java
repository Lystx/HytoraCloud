
package cloud.hytora.driver.component.displays.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public class ScoreboardEntry {

    /**
     * The score of this item
     */
    private final int score;

    /**
     * The value to display
     */
    private final String value;

    /**
     * Returns the item value
     *
     * @return the item value
     */
    public String value() {
        return value;
    }

    /**
     * Returns the item score
     *
     * @return the item score
     */
    public int score() {
        return score;
    }

}
