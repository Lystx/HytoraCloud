package cloud.hytora.script.api;

import cloud.hytora.common.task.IPromise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public interface IScript {

    void putVariable(@NotNull String name, @NotNull String value);

    @Nullable
    String getVariable(@NotNull String name);

    @NotNull
    Map<String, String> getVariables();

    @NotNull
    String replaceVariables(@NotNull  String line);

    @NotNull
    IScriptLoader getLoader();


    @NotNull
    Collection<IScriptTask> getAllTasks();

    @NotNull
    Path getScriptPath();

    IPromise<Void> executeAsync();

    void execute();
}
