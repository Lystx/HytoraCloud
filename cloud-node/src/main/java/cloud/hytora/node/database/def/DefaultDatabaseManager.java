package cloud.hytora.node.database.def;

import cloud.hytora.common.task.Task;


import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.node.database.config.DatabaseConfiguration;
import cloud.hytora.node.database.config.DatabaseType;
import cloud.hytora.driver.database.IDatabase;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.node.database.impl.DatabaseFile;
import cloud.hytora.node.database.impl.DatabaseMongoDB;
import cloud.hytora.node.database.impl.DatabaseMySQL;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class DefaultDatabaseManager implements IDatabaseManager {

    private final IDatabase internalDatabase;
    private SectionedDatabase database;

    public DefaultDatabaseManager(DatabaseType type, DatabaseConfiguration configuration) {
        if (type == DatabaseType.MYSQL) {
            this.internalDatabase = new DatabaseMySQL(configuration);
        } else if (type == DatabaseType.MONGODB) {
            this.internalDatabase = new DatabaseMongoDB(configuration);
        } else {
            this.internalDatabase = new DatabaseFile(configuration);
        }
        //database cannot be null
        this.internalDatabase.connect();
        this.database = new SectionedDatabase(this.internalDatabase);
    }

    @Override
    public @NotNull Task<Boolean> shutdown() {
        internalDatabase.disconnect();
        return Task.newInstance(true);
    }


}
