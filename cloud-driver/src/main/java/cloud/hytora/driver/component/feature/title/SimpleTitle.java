package cloud.hytora.driver.component.feature.title;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.base.SimpleTextBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @AllArgsConstructor @Setter
public class SimpleTitle extends SimpleTextBase<Title> implements Title {

    /**
     * The title
     */
    private String title;

    /**
     * The subtitle
     */
    private String subTitle;

    /**
     * The fade-in time
     */
    private int fadeIn;

    /**
     * The stay-time
     */
    private int stay;

    /**
     * The fade-out time
     */
    private int fadeOut;

    public SimpleTitle() {
        this.addHandler(option -> {
            if (option.getName().equalsIgnoreCase("title")) {
                title = option.getValue(Component.class, Component.empty()).getContent();
            } else if (option.getName().equalsIgnoreCase("subtitle")) {
                subTitle = option.getValue(Component.class, Component.empty()).getContent();
            } else if (option.getName().equalsIgnoreCase("fadeIn")) {
                fadeIn = option.getValue(int.class, 1);
            } else if (option.getName().equalsIgnoreCase("stay")) {
                stay = option.getValue(int.class, 1);
            } else if (option.getName().equalsIgnoreCase("fadeOut")) {
                fadeOut = option.getValue(int.class, 1);
            }
        });
    }
}
