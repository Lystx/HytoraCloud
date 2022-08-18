package cloud.hytora.script.api;

import java.util.Collection;

/**
 * Represents a task in a reform script. It's like a method in java code
 */
public interface IScriptTask {

    String getName();

    void executeTask(String callerLine, IScript script, Collection<String> allLines);
}
