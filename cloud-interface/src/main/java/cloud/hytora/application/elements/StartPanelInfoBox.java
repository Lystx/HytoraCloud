package cloud.hytora.application.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@AllArgsConstructor
@Getter
@Setter
public class StartPanelInfoBox {


    private int id;
    private String title;
    private String content;
    private String resourceIcon;
    private Supplier<String> textUpdater;
}
