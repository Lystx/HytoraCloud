package cloud.hytora.driver.commands.parameter;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.driver.commands.exceptions.InvalidCommandUsageException;
import lombok.Getter;
import cloud.hytora.driver.commands.data.CommandFlag;
import cloud.hytora.driver.commands.data.DriverCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class of command arguments
 */
public class CommandArguments extends AbstractBundledParameters {

    /**
     * Instance of the command
     */
    @Getter
    private final DriverCommand command;

    /**
     * The raw parameter
     */
    @Getter
    private final List<String> baseParameter;

    /**
     * The found flags
     */
    private final Map<String, CommandFlag> flags = new HashMap<>();

    public CommandArguments(DriverCommand driverCommand, String[] args) {
        super(String.join(" ", args));
        this.baseParameter = new ArrayList<>(super.getParameter());
        if(baseParameter.size() >= 1 && baseParameter.get(0).isEmpty()) baseParameter.remove(0);
        this.command = driverCommand;

        // list flags
        int lastIndex = -1;
        CommandFlag flag = null;
        for(int i = 0; i < baseParameter.size(); i++) {
            String param = baseParameter.get(i);
            if(flag != null) flag.getParameter().add(param);

            // checks if the command instance either contains the flag or
            // if the flag is the help flag
            String flagLabel = param.replaceFirst("-", "");
            boolean hasFlag = driverCommand.getFlags().contains(flagLabel) || CollectionUtils.contains(DriverCommand.PREDEFINED_FLAGS, flagLabel);
            if(param.startsWith(CommandFlag.SPECIFIER) && hasFlag) {
                if(lastIndex == -1) lastIndex = i;

                flag = new CommandFlag(param.substring(1, param.length()));
                flags.put(flag.getLabel(), flag);
            }
        }

        // list parameter
        this.setParameter(baseParameter);
        if(lastIndex != -1) this.setParameter(baseParameter.subList(0, lastIndex));
    }

    @Override
    public <T> T get(int paramIndex, Class<T> tClass, T backupVal) {
        if(paramIndex >= size() && backupVal == null) {
            throw new InvalidCommandUsageException(InvalidCommandUsageException.Type.TOO_FEW_ARGUMENTS, command);
        }
        return super.get(paramIndex, tClass, backupVal);
    }

    /**
     * Get all flags
     *
     * @return The list of flags
     */
    public List<CommandFlag> getFlags() {
        return new ArrayList<>(flags.values());
    }

    /**
     * Gets the flag with given key
     *
     * @param key The key
     * @return The flag
     */
    public CommandFlag getFlag(String key) {
        return flags.get(key);
    }

    /**
     * Checks if the paramater set has given key
     *
     * @param key The key
     * @return The result
     */
    public boolean hasFlag(String key) {
        return flags.containsKey(key);
    }

}