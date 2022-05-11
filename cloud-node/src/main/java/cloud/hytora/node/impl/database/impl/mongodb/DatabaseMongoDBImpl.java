package cloud.hytora.node.impl.database.impl.mongodb;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.database.DatabaseConfiguration;
import cloud.hytora.node.impl.database.IDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DatabaseMongoDBImpl implements IDatabase {

    private MongoClient client;
    private MongoDatabase database;

    @Override
    public void connect() {
        DatabaseConfiguration configuration = MainConfiguration.getInstance().getDatabaseConfiguration();
        String uri = null;
        if(configuration.getUser().isEmpty() && configuration.getPassword().isEmpty()){
            uri = "mongodb://" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + configuration.getAuthDatabase();
        }else{
            uri = "mongodb://" + configuration.getUser() + ":" + configuration.getPassword() + "@" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + config.getAuthDatabase();
        }
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        this.client = MongoClients.create(settings);
        this.database = this.client.getDatabase(configuration.getDatabase());
    }

    @Override
    public @NotNull Wrapper<Boolean> disconnect() {
        Wrapper<Boolean> shutdownPromise = Wrapper.empty();
        if(this.client != null) {
            this.client.close();
        }
        shutdownPromise.setResult(true);
        return shutdownPromise;
    }

    @Override
    public void addGroup(@NotNull ServerConfiguration serviceGroup) {

    }

    @Override
    public void removeGroup(@NotNull ServerConfiguration serviceGroup) {

    }

    @Override
    public List<ServerConfiguration> getAllServiceGroups() {
        return null;
    }

}
