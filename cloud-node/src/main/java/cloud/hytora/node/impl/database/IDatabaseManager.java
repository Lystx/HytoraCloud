package cloud.hytora.node.impl.database;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    IDatabase getInternalDatabase();

    SectionedDatabase getDatabase();

    @NotNull
    Wrapper<Boolean> shutdown();

}
