package cloud.hytora.driver.script.commands;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.script.ScriptCommand;
import cloud.hytora.driver.script.ScriptLoader;

public class LogScriptCommand implements ScriptCommand {

    @Override
    public void execute(String[] args, String input, String commandLine) {
        String type = args[1];

        String message = input.split(type)[1];

        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.replaceFirst("\"", "");
            message = StringUtils.replaceLast(message, "\"", "");
        }

        message = ScriptLoader.getInstance().replaceScriptVariables(message.trim());
        message = ConsoleColor.toColoredString('&', message);

        if (type.equalsIgnoreCase("INFO")) {
            CloudDriver.getInstance().getLogger().info(message);
        } else if (type.equalsIgnoreCase("WARN")) {
            CloudDriver.getInstance().getLogger().warn(message);
        } else if (type.equalsIgnoreCase("DEBUG")) {
            CloudDriver.getInstance().getLogger().debug(message);
        } else if (type.equalsIgnoreCase("ERROR")) {
            CloudDriver.getInstance().getLogger().error(message);
        } else if (type.equalsIgnoreCase("TRACE")) {
            CloudDriver.getInstance().getLogger().trace(message);
        } else {
            throw new IllegalArgumentException("Can't log for given type '" + type + "'");
        }

    }
}
