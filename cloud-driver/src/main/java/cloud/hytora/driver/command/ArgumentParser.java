package cloud.hytora.driver.command;

/**
 * An {@link ArgumentParser} parses String-Based-Inputs to your chosen object.
 * This is needed to simplify the use of {@link cloud.hytora.driver.command.annotation.Command.Argument}
 *
 * @param <T> the obejct of your choice
 */
public interface ArgumentParser<T> {

    /**
     * Parses the chosen object-type from String input
     *
     * @param input the command input
     * @return parsed obejct
     * @throws Exception if something went wrong during parsing
     */
    T parse(String input) throws Exception;
}
