package cloud.hytora.node.impl.database.cloud;

import cloud.hytora.common.task.Task;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.IDatabaseConfig;
import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.database.LocalStorage;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.action.SQLColumn;
import cloud.hytora.driver.language.Translation;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.DefaultJsonStorage;
import cloud.hytora.node.impl.database.mongo.MongoDBDatabase;
import cloud.hytora.node.impl.database.sql.mysql.MySQLDatabase;
import cloud.hytora.node.impl.database.sql.sqlite.SQLiteDatabase;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Getter
public class DefaultDatabaseManager implements IDatabaseManager {

    private final Database database;
    private final LocalStorage localStorage;

    public DefaultDatabaseManager(DatabaseType type, IDatabaseConfig config) {
        DatabaseConfig configuration = new DatabaseConfig(
                config.getHost(),
                config.getDatabase(),
                config.getAuthDatabase(),
                config.getPassword(),
                config.getUser(),
                config.getPort(),
                true,
                null
        );
        if (type == DatabaseType.MYSQL) {
            this.database = new MySQLDatabase(configuration);
        } else if (type == DatabaseType.MONGODB) {
            this.database = new MongoDBDatabase(configuration);
        /*} else if (type == DatabaseType.FILE) {
            //this.internalDatabase = new DatabaseFile(configuration);*/
        } else  {
            configuration.setFile(new File(CloudDriver.Constants.DATABASE_FOLDER, "cloud_network_database.db"));
            this.database = new SQLiteDatabase(configuration);
        }
        //database cannot be null
        try {
            this.database.connect();
            if (database.getConfig().getHost() != null) {
                CloudDriver.getInstance().getLogger().info(Translation.of("database.connect.success.online"), database.getConfig().getHost(), database.getConfig().getPort(), database.getConfig().getDatabase());
            } else {
                CloudDriver.getInstance().getLogger().info(Translation.of("database.connect.success.local"), database.getConfig().getHost(), database.getConfig().getPort(), database.getConfig().getDatabase());
            }
            this.setupTables();
        } catch (Exception e) {
            CloudDriver.getInstance().getLogger().error(Translation.of("database.connect.failed"), config.getType());
            e.printStackTrace();
        }

        this.localStorage = new LocalStorage(new DefaultJsonStorage());
    }


    private void setupTables() throws Exception {
        if (this.database.isConnected()) {
            this.database.createTable(
                    "player_data",
                    new SQLColumn("name", SQLColumn.Type.TEXT, 16),
                    new SQLColumn("uniqueId", SQLColumn.Type.TEXT, 36),
                    new SQLColumn("firstLogin", SQLColumn.Type.INT, 64),
                    new SQLColumn("lastLogin", SQLColumn.Type.INT, 64),
                    new SQLColumn("properties", SQLColumn.Type.LONGTEXT, Integer.MAX_VALUE)
            );
        }
    }

    @Override
    public @NotNull Task<Boolean> shutdown() {
        return Task.callSync(() -> {
            database.disconnect();
            return true;
        });
    }


}
