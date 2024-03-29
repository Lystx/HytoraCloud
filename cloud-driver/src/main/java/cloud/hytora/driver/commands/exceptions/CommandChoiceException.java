package cloud.hytora.driver.commands.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandChoiceException extends RuntimeException {

    private Type type;

    public enum Type {

        TIMEOUT,
        CANCELLED,
        NO_CHOICE

    }

}
