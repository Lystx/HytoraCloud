package cloud.hytora.database.handler;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.database.DocumentDatabase;
import cloud.hytora.document.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class DatabaseHelper {

    public static File getDir() {
        return DocumentDatabase.getInstance().getDirectory();
    }

    public static File createDatabase(String name) {
        File dir = new File(getDir(), name.toUpperCase() + "/");
        dir.mkdirs();
        File configFile = new File(dir, "database" + DocumentDatabase.EXTENSION);

        try {
            Document.newJsonDocument(
                    "name", name,
                    "lastModifiedAt", System.currentTimeMillis()
            ).saveToFile(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static Boolean existsDatabase(String name) {
        return new File(getDir(), name.toUpperCase() + "/").exists();
    }

    public static Collection<String> listCollections(String database) {
        File[] files = createDatabase(database).listFiles();
        if (files != null) {
            return Arrays.stream(files).filter(File::isDirectory).map(f -> f.getName().replace(DocumentDatabase.EXTENSION, "")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static File createCollection(String databaseName, String collectionName) {
        File dir = createDatabase(databaseName);
        File folder = new File(dir, collectionName.toUpperCase() + "/");
        folder.mkdirs();

        return folder;
    }

    public static void deleteCollection(String databaseName, String collectionName) {
        File collection = createCollection(databaseName, collectionName);
        FileUtils.delete(collection.toPath());
    }

    public static Boolean existsCollection(String databaseName, String collectionName) {
        return new File(new File(getDir(), databaseName.toUpperCase() + "/"), collectionName.toUpperCase() + "/").exists();
    }

    public static Boolean existsEntry(String databaseName, String collectionName, String identifier) {
        return findEntry(databaseName, collectionName, identifier) != null;
    }

    public static Document findEntry(String databaseName, String collectionName, String identifier) {
        File collection = createCollection(databaseName, collectionName);
        File entryFile = new File(collection, identifier + DocumentDatabase.EXTENSION);
        return entryFile.exists() ? Document.newJsonDocumentUnchecked(entryFile) : null;
    }

    public static void deleteEntry(String databaseName, String collectionName, String identifier) {

        File collection = createCollection(databaseName, collectionName);
        File entryFile = new File(collection, identifier + DocumentDatabase.EXTENSION);
        entryFile.delete();
    }

    public static Collection<String> getIdentifiers(String databaseName, String collectionName) {

        File collection = createCollection(databaseName, collectionName);
        File[] files = collection.listFiles();

        return files == null ? new ArrayList<>() : Arrays.stream(files).map(f -> f.getName().replace(DocumentDatabase.EXTENSION, "")).collect(Collectors.toList());
    }

    public static void upsertEntry(String databaseName, String collectionName, Document document, String id) {
        File collection = createCollection(databaseName, collectionName);
        File entryFile = new File(collection, id + DocumentDatabase.EXTENSION);
        try {
            document.saveToFile(entryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
