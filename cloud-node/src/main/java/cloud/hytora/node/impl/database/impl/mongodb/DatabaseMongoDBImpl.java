package cloud.hytora.node.impl.database.impl.mongodb;

import cloud.hytora.common.collection.SeededRandomWrapper;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.SimpleServerConfiguration;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.database.DatabaseConfiguration;
import cloud.hytora.node.impl.database.IDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMongoDBImpl implements IDatabase {

    private MongoClient client;
    private MongoDatabase database;

    private String groupsCollectionName = "cloudsystem_groups";
    private MongoCollection groupsCollection;

    @Override
    public void connect() {
        DatabaseConfiguration configuration = MainConfiguration.getInstance().getDatabaseConfiguration();
        String uri;
        if(configuration.getUser().isEmpty() && configuration.getPassword().isEmpty()){
            uri = "mongodb://" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + configuration.getAuthDatabase();
        }else{
            uri = "mongodb://" + configuration.getUser() + ":" + configuration.getPassword() + "@" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + configuration.getAuthDatabase();
        }
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        this.client = MongoClients.create(settings);
        this.database = this.client.getDatabase(configuration.getDatabase());
        this.groupsCollection = this.database.getCollection(this.groupsCollectionName);
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
        Document data = new Document();
        data.append("name", serviceGroup.getName())
            .append("template", serviceGroup.getTemplate())
            .append("node", serviceGroup.getNode())
            .append("memory", serviceGroup.getMemory())
            .append("minOnlineService", serviceGroup.getMinOnlineService())
            .append("maxOnlineService", serviceGroup.getMaxOnlineService())
            .append("shutdownBehaviour", serviceGroup.getShutdownBehaviour().name())
            .append("fallbackGroup", serviceGroup.getFallback().isEnabled())
            .append("version", serviceGroup.getVersion().name())
            .append("maxPlayers", serviceGroup.getDefaultMaxPlayers())
            .append("motd", serviceGroup.getMotd());

        this.groupsCollection.createIndex(Filters.eq("name", serviceGroup.getName()));
    }

    @Override
    public void removeGroup(@NotNull ServerConfiguration serviceGroup) {
        this.groupsCollection.dropIndex(Filters.eq("name", serviceGroup.getName()));
    }

    @Override
    public List<ServerConfiguration> getAllServiceGroups() {
        FindIterable<Document> documents = this.groupsCollection.find();
        List<ServerConfiguration> serverConfigurations = new ArrayList<>();
        for (Document document : documents) {
            //set data
        }
        return serverConfigurations;
    }

}
