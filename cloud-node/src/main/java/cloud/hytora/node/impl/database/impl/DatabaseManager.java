package cloud.hytora.node.impl.database.impl;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.node.impl.database.DatabaseType;
import cloud.hytora.node.impl.database.impl.file.DatabaseFile;
import cloud.hytora.node.impl.database.impl.sql.DatabaseSqlImpl;
import cloud.hytora.node.impl.database.IDatabase;
import cloud.hytora.node.impl.database.IDatabaseManager;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class DatabaseManager implements IDatabaseManager {

    @Getter
    private final IDatabase database;

    public DatabaseManager(DatabaseType type) {

        if (type == DatabaseType.MYSQL) {
            this.database = new DatabaseSqlImpl();
        } else if (type == DatabaseType.FILE) {
            this.database = new DatabaseFile();
        } else {
            this.database = null;
        }
        this.database.connect();
    }

    public @NotNull Wrapper<Boolean> shutdown() {
        return this.database.disconnect();
    }


}
