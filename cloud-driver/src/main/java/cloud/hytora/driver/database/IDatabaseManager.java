package cloud.hytora.driver.database;

import cloud.hytora.common.task.IPromise;
import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    IDatabase getInternalDatabase();

    SectionedDatabase getDatabase();

    @NotNull
    IPromise<Boolean> shutdown();

}
