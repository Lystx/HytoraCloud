package cloud.hytora.driver.component.feature.title;

import cloud.hytora.driver.component.base.TextBased;

public interface Title extends TextBased<Title> {

    /**
     * The main title of this title
     */
    String getTitle();

    /**
     * The subtitle of this title
     */
    String getSubTitle();

    /**
     * The time this title fades in
     */
    int getFadeIn();

    /**
     * The time this title stays
     */
    int getStay();

    /**
     * The time this title fades out
     */
    int getFadeOut();
}
