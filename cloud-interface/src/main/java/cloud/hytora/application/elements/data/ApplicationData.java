package cloud.hytora.application.elements.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationData {


    @Getter
    private static ApplicationData current;

    private int lastOpenedTab;
    private CloudTheme theme;


    public ApplicationData(int lastOpenedTab, CloudTheme theme) {
        this.lastOpenedTab = lastOpenedTab;
        this.theme = theme;

        current = this;
    }
}
