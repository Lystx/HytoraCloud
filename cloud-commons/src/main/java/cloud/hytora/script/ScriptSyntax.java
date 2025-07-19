package cloud.hytora.script;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;

public class ScriptSyntax {

    public static final String COMMENT_LINE_START = "#";
    public static final String MULTI_COMMENT_LINE_START = "/*";
    public static final String MULTI_COMMENT_LINE_END = "*/";

    public static final String VARIABLE_BRACKET_RIGHT = "$";
    public static final String VARIABLE_BRACKET_LEFT = "$";

    public static final String TASK_LINE_START = "Task ";
    public static final String TASK_END = "} :";


    public static String formatCommandLine(IScript script, IScriptCommand command, String commandLine) {
        commandLine = commandLine.replaceFirst(command.getCommand() + " ", "");
        commandLine = commandLine.replaceFirst("\"", "");
        commandLine = StringUtils.replaceLast(commandLine, "\"", "");
        commandLine = script.replaceVariables(commandLine);

        return commandLine;
    }
}
