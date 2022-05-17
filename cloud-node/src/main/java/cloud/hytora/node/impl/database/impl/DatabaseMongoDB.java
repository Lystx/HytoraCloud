package cloud.hytora.node.impl.database.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import cloud.hytora.node.impl.database.IDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DatabaseMongoDB implements IDatabase {

    private MongoClient client;
    private MongoDatabase database;
    private final String identifier = "_uniqueId_key_";

    @Override
    public void connect() {
        DatabaseConfiguration configuration = MainConfiguration.getInstance().getDatabaseConfiguration();
        String uri;
        if (configuration.getUser().isEmpty() && configuration.getPassword().isEmpty()) {
            uri = "mongodb://" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + configuration.getAuthDatabase();
        } else {
            uri = "mongodb://" + configuration.getUser() + ":" + configuration.getPassword() + "@" + configuration.getHost() + ":" + configuration.getPort() + "/?authSource=" + configuration.getAuthDatabase();
        }
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        this.client = MongoClients.create(settings);
        this.database = this.client.getDatabase(configuration.getDatabase());
    }

    @Override
    public void disconnect() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public void insert(String collection, String key, Document document) {
        document.set(identifier, key);

        MongoCollection<org.bson.Document> col = this.database.getCollection(collection);
        col.insertOne(document.asBsonDocument().getWrapper());
    }

    @Override
    public void update(String collection, String key, Document document) {
        document.set(identifier, key);

        MongoCollection<org.bson.Document> col = this.database.getCollection(collection);
        org.bson.Document first = col.find(Filters.eq(identifier, key)).first();
        if (first != null) {
            col.updateOne(first, document.asBsonDocument().getWrapper());
        }
    }

    @Override
    public boolean contains(String collection, String key) {
        return entries(collection).containsKey(key);
    }

    @Override
    public void delete(String collection, String key) {
        org.bson.Document first = this.database.getCollection(collection).find(Filters.eq(identifier, key)).first();
        if (first == null) {
            return;
        }
        this.database.getCollection(collection).deleteOne(first);
    }

    @Override
    public Document get(String collection, String key) {
        return entries(collection).get(key);
    }

    @Override
    public Collection<Document> get(String collection, String fieldName, Object fieldValue) {
        FindIterable<org.bson.Document> documents = this.database.getCollection(collection).find(Filters.eq(fieldName, fieldValue));
        ArrayList<org.bson.Document> into = documents.into(new ArrayList<>());
        return into.stream().map(b -> DocumentFactory.newJsonDocument(b.toJson())).collect(Collectors.toList());
    }

    @Override
    public Collection<String> keys(String collection) {
        return entries(collection).keySet();
    }

    @Override
    public Collection<Document> documents(String collection) {
        return entries(collection).values();
    }

    @Override
    public Map<String, Document> entries(String collection) {
        return filter(collection, ((s, document) -> true));
    }

    @Override
    public Map<String, Document> filter(String collection, BiPredicate<String, Document> predicate) {
        Map<String, Document> map = new HashMap<>();
        for (org.bson.Document bson : this.database.getCollection(collection).find()) {
            Document document = DocumentFactory.newJsonDocument(bson.toJson());
            String key = document.getString(identifier);
            if (predicate.test(key, document)) {
                map.put(key, document);
            }
        }
        return map;
    }

    @Override
    public void iterate(String collection, BiConsumer<String, Document> consumer) {
        entries(collection).forEach(consumer);
    }

    @Override
    public void clear(String collection) {

    }
}
