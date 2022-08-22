package cloud.hytora.node.impl.database.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.node.NodeDriver;


import cloud.hytora.driver.database.IDatabase;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DatabaseFile implements IDatabase {

    private final String fileExtension = ".json";

    public DatabaseFile() {
    }

    public DatabaseFile(DatabaseConfiguration configuration) {

    }

    @Override
    public void connect() {}

    @Override
    public void disconnect() {}
    
    private File checkCollection(String collection) {

        File collectionFolder = new File(NodeDriver.STORAGE_FOLDER, collection + "/");
        try {

            if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE && !NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
                return collectionFolder;
            }
        } catch (Exception e) {
            //ignored
        }
        collectionFolder.mkdirs();
        
        return collectionFolder;
    }

    @Override
    public void insert(String collection, String key, Document document) {
        File entryFile = new File(this.checkCollection(collection), key + fileExtension);
        try {
            document.saveToFile(entryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String collection, String key, Document document) {
        File entryFile = new File(this.checkCollection(collection), key + fileExtension);
        try {
            document.saveToFile(entryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(String collection, String key) {
        File entryFile = new File(this.checkCollection(collection), key + fileExtension);
        return entryFile.exists();
    }

    @Override
    public void delete(String collection, String key) {
        File entryFile = new File(this.checkCollection(collection), key + fileExtension);
        entryFile.delete();
    }

    @Override
    public Document byId(String collection, String key) {
        File entryFile = new File(this.checkCollection(collection), key + fileExtension);
        try {
            return DocumentFactory.newJsonDocument(entryFile);
        } catch (IOException e) {
            return DocumentFactory.newJsonDocument();
        }
    }

    @Override
    public Collection<Document> filter(String collection, String fieldName, Object fieldValue) {
        return documents(collection).stream().filter(d -> d.get(fieldName).toString().equalsIgnoreCase(fieldValue.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<String> keys(String collection) {
        File[] files = this.checkCollection(collection).listFiles();
        return Arrays.stream(files == null ? new File[0] : files).map(f -> f.getName().split(fileExtension)[0]).collect(Collectors.toList());
    }

    @Override
    public Collection<Document> documents(String collection) {
        File[] files = this.checkCollection(collection).listFiles();
        return Arrays.stream(files == null ? new File[0] : files).map(DocumentFactory::newJsonDocumentUnchecked).collect(Collectors.toList());
    }

    @Override
    public Map<String, Document> entries(String collection) {
        return filter(collection, (s, document) -> true);
    }

    @Override
    public Map<String, Document> filter(String collection, BiPredicate<String, Document> predicate) {
        File[] files = this.checkCollection(collection).listFiles();
        Map<String, Document> map = new HashMap<>();
        for (File file : (files == null ? new File[0] : files)) {
            String key = file.getName().split(fileExtension)[0];
            Document document = DocumentFactory.newJsonDocumentUnchecked(file);
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
        File file = this.checkCollection(collection);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
