package cloud.hytora.node.impl.database.cloud;

import cloud.hytora.common.task.Task;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.LocalStorage;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.action.SQLColumn;
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

    public DefaultDatabaseManager(DatabaseType type, DatabaseConfiguration config) {
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
            configuration.setFile(new File(NodeDriver.DATABASE_FOLDER, "cloud_network_database.db"));
            this.database = new SQLiteDatabase(configuration);
        }
        //database cannot be null
        try {
            this.database.connect();
            CloudDriver.getInstance().getLogger().info("§7Database has connected §asuccessfully §8[§aHost§8: §7{} §aPort§8: §7{} §aDatabase§8: §7{}§8]", database.getConfig().getHost(), database.getConfig().getPort(), database.getConfig().getDatabase());
            this.setupTables();
        } catch (Exception e) {
            CloudDriver.getInstance().getLogger().error("§cCouldn't connect to database of type §e{}", config.getType());
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
