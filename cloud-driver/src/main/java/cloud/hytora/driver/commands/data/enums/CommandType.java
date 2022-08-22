package cloud.hytora.driver.commands.data.enums;

public enum CommandType {

    /**
     * Root of a command (e.g. /fly [sub] where fly would be the root)
     */
    ROOT,

    /**
     * Sub of a command (e.g. /fly [sub] where sub would be the subcommand)
     */
    SUB,

    /**
     * ..
     */
    UNKNOWN

}
