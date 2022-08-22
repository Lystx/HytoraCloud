package cloud.hytora.driver.commands.exceptions;

import cloud.hytora.driver.commands.data.DriverCommand;
import lombok.Getter;

/**
 * Error if the command usage is invalid, e.g. too few arguments
 *
 * @see Type
 */
@Getter
public class InvalidCommandUsageException extends RuntimeException {

    private Type type;
    private DriverCommand command;
    private Object[] helpParams;

    public InvalidCommandUsageException(Type type, DriverCommand command, Object... helpParams) {
        super("Invalid usage: " + type);
        this.type = type;
        this.command = command;
        this.helpParams = helpParams;
    }

    public enum Type {

        /**
         * The player used too few arguments
         */
        TOO_FEW_ARGUMENTS,

        /**
         * The player is not allowed to use this command
         */
        NOT_ALLOWED,

        /**
         * Custom wrong usage message with triggering the ArgumentHelper
         */
        CUSTOM_EVENTABLE

    }


}
