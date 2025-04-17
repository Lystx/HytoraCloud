package cloud.hytora.driver.database;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.database.api.Database;
import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    Database getDatabase();

    LocalStorage getLocalStorage();

    @NotNull
    Task<Boolean> shutdown();

}
