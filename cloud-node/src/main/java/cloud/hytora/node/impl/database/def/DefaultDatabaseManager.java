package cloud.hytora.node.impl.database.def;

import cloud.hytora.common.wrapper.Wrapper;


import cloud.hytora.node.impl.database.impl.CloudDatabase;
import cloud.hytora.node.impl.database.config.DatabaseType;
import cloud.hytora.node.impl.database.IDatabase;
import cloud.hytora.node.impl.database.IDatabaseManager;
import cloud.hytora.node.impl.database.impl.DatabaseFile;
import cloud.hytora.node.impl.database.impl.DatabaseMongoDB;
import cloud.hytora.node.impl.database.impl.DatabaseMySQL;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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
