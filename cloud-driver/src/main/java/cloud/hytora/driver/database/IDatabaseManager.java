package cloud.hytora.driver.database;

import cloud.hytora.common.task.Task;
import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    IDatabase getInternalDatabase();

    SectionedDatabase getDatabase();

    @NotNull
    Task<Boolean> shutdown();

}
