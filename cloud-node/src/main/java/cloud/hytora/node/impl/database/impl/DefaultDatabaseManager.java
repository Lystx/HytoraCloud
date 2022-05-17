package cloud.hytora.node.impl.database.impl;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.node.impl.database.CloudDatabase;
import cloud.hytora.node.impl.database.DatabaseType;
import cloud.hytora.node.impl.database.IDatabaseManager;


import cloud.hytora.node.impl.database.impl.file.DatabaseFile;
import cloud.hytora.node.impl.database.impl.mongodb.DatabaseMongoDB;
import cloud.hytora.node.impl.database.impl.sql.DatabaseMySQL;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

@Getter
public class DefaultDatabaseManager implements IDatabaseManager {

    private final IDatabase internalDatabase;
    private final CloudDatabase database;

    public DefaultDatabaseManager(DatabaseType type) {
        if (type == DatabaseType.MYSQL) {
            this.internalDatabase = new DatabaseMySQL();
        } else if (type == DatabaseType.MONGODB) {
            this.internalDatabase = new DatabaseMongoDB();
        } else if (type == DatabaseType.FILE) {
            this.internalDatabase = new DatabaseFile();
        } else {
            this.internalDatabase = null;
        }
        //database cannot be null
        this.internalDatabase.connect();
        this.database = new CloudDatabase(this.internalDatabase);
    }

    @Override
    public @NotNull Wrapper<Boolean> shutdown() {
        return Wrapper.callSync(() -> {
            internalDatabase.disconnect();
            return true;
        });
    }


}
