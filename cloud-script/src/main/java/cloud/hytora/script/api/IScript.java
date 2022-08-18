package cloud.hytora.script.api;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

public interface IScript {

    @NotNull
    IScriptLoader getLoader();

    @NotNull
    Collection<IScriptTask> getAllTasks();

    @NotNull
    Path getScriptPath();

    void execute();
}
