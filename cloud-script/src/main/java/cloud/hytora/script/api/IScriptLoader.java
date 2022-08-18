package cloud.hytora.script.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface IScriptLoader {


    @NotNull
    IScriptLoader registerCommand(@NotNull IScriptCommand command);

    @Nullable
    IScriptCommand getCommand(@NotNull String command);

    @Nullable
    IScript loadScript(@NotNull Path script);
}
