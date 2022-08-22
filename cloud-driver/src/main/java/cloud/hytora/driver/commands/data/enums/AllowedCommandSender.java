package cloud.hytora.driver.commands.data.enums;


public enum AllowedCommandSender {

    /**
     * Not needed but with minecraft
     * the user is the player who executes a command
     */
    USER,

    /**
     * The console who executes the command.
     */
    CONSOLE,

    /**
     * Either {@link #USER} or {@link #CONSOLE} is allowed
     */
    BOTH

}
