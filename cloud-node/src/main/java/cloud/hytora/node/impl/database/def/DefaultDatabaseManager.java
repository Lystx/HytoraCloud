package cloud.hytora.node.impl.database.def;

import cloud.hytora.common.wrapper.Task;


import cloud.hytora.node.impl.database.impl.SectionedDatabase;
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
    private final SectionedDatabase database;

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

        this.database = new SectionedDatabase(this.internalDatabase);
    }

    @Override
    public @NotNull Task<Boolean> shutdown() {
        return Task.callSync(() -> {
            internalDatabase.disconnect();
            return true;
        });
    }


}
