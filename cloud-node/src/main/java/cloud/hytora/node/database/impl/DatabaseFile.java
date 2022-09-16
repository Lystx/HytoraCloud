package cloud.hytora.node.database.impl;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.database.DatabaseDriver;
import cloud.hytora.database.DocumentDatabase;
import cloud.hytora.database.api.elements.Database;
import cloud.hytora.database.api.elements.DatabaseCollection;
import cloud.hytora.database.api.elements.DatabaseEntry;
import cloud.hytora.database.api.elements.DatabaseFilter;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.node.NodeDriver;


import cloud.hytora.driver.database.IDatabase;
import cloud.hytora.node.database.config.DatabaseConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DatabaseFile implements IDatabase {

    private final DatabaseDriver driver;
    private final DatabaseConfiguration configuration;
    private Database database;

    public DatabaseFile(DatabaseConfiguration configuration) {
        this.configuration = configuration;
        this.driver = new DatabaseDriver(CloudDriver.getInstance().getNetworkExecutor().getName());
        if (configuration.getPassword().equalsIgnoreCase("local") || configuration.getHost().equalsIgnoreCase("127.0.0.1")) {
            Task.runAsync(() -> {
                DocumentDatabase db = new DocumentDatabase(
                        Logger.constantInstance(),
                        NodeDriver.getInstance().getWebServer(),
                        new File(NodeDriver.STORAGE_FOLDER, "database/"),
                        false,
                        configuration.getPassword()
                );

            });
        }
    }

    @Override
    public void connect() {
        Task.runAsync(() -> {
            this.driver.connect(new HttpAddress(configuration.getHost(), configuration.getPort()), configuration.getPassword());
            this.database = this.driver.getDatabase(configuration.getDatabase());
        });
    }

    @Override
    public void disconnect() {
        this.driver.close();
    }

    @Override
    public void insert(String collection, String key, Document document) {

        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        db.insertEntryAsync(e -> {
            e.setId(key);
            e.setDocument(document);
        });
    }

    @Override
    public void update(String collection, String key, Document document) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        db.updateEntryAsync(key, e -> {
            e.setId(key);
            e.setDocument(document);
        });
    }

    @Override
    public boolean contains(String collection, String key) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        return db.hasEntry(key);
    }

    @Override
    public void delete(String collection, String key) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        db.deleteEntryAsync(key);
    }

    @Override
    public Document byId(String collection, String key) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        return db.findEntry(key);
    }

    @Override
    public Collection<Document> filter(String collection, String fieldName, Object fieldValue) {
        return documents(collection).stream().filter(d -> d.get(fieldName).toString().equalsIgnoreCase(fieldValue.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<String> keys(String collection) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        return db.getIdentifiers();
    }

    @Override
    public Collection<Document> documents(String collection) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        return db.findEntries().stream().map(e -> (Document)e).collect(Collectors.toList());
    }

    @Override
    public Map<String, Document> entries(String collection) {
        return filter(collection, (s, document) -> true);
    }

    @Override
    public Map<String, Document> filter(String collection, BiPredicate<String, Document> predicate) {
        DatabaseCollection db = this.database.getCollectionOrCreate(collection);
        Map<String, Document> map = new HashMap<>();
        for (DatabaseEntry entry : db.findEntries()) {
            if (predicate.test(entry.getId(), entry)) {
                map.put(entry.getId(), entry);
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
