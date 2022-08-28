package cloud.hytora.driver.database;

import cloud.hytora.common.task.ITask;
import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    IDatabase getInternalDatabase();

    SectionedDatabase getDatabase();

    @NotNull
    ITask<Boolean> shutdown();

}
