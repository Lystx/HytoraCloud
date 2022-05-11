package cloud.hytora.node.impl.database;

import cloud.hytora.common.wrapper.Wrapper;

import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {

    @NotNull
    IDatabase getDatabase();

    @NotNull
    Wrapper<Boolean> shutdown();

}
