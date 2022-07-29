package cloud.hytora.driver.console;

import lombok.Setter;

import java.util.Collections;
import java.util.function.Consumer;

public class EditableConsoleLine {

    private final Consumer<String> printer;

    public EditableConsoleLine(Consumer<String> printer) {
        this.printer = printer;
    }


    public void display(String text) {

        String string = "\r" + text;

        this.printer.accept(string);
    }
}
