package cloud.hytora.node.database.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.node.config.MainConfiguration;
import cloud.hytora.node.database.config.DatabaseConfiguration;
import cloud.hytora.driver.database.IDatabase;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DatabaseMySQL implements IDatabase {

    private Connection connection;

    public DatabaseMySQL(DatabaseConfiguration config) {

    }

    @SneakyThrows
    @Override
    public void connect() {
        DatabaseConfiguration databaseConfiguration = MainConfiguration.getInstance().getDatabaseConfiguration();
        this.connection = DriverManager.getConnection("jdbc:mysql://" + databaseConfiguration.getHost() + ":" + databaseConfiguration.getPort()
                        + "/" + databaseConfiguration.getDatabase() + "?useUnicode=true&autoReconnect=true",
                databaseConfiguration.getUser(), databaseConfiguration.getPassword());

        CloudDriver.getInstance().getLogger().info("The connection is now established to the database.");
    }


    @Override
    @SneakyThrows
    public void disconnect() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    private void checkTable(String name) {
        //create tables
        this.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + "(key VARCHAR(100), document VARCHAR(100000))");
    }


    @Override
    public void insert(String collection, String key, Document document) {
        this.checkTable(collection);
        this.executeUpdate("INSERT INTO " + collection + "(key, document) VALUES ('" + key + "', '" + document.asRawJsonString() + "');");
    }

    @Override
    public void update(String collection, String key, Document document) {
        this.checkTable(collection);
        this.executeUpdate("UPDATE '" + collection + "' SET document = '" + document.asRawJsonString() + "' WHERE key LIKE '" + key + "'");
    }

    @Override
    public boolean contains(String collection, String key) {
        this.checkTable(collection);
        return this.entries(collection).containsKey(key);
    }

    @Override
    public void delete(String collection, String key) {
        this.checkTable(collection);
        this.executeUpdate("DELETE FROM " + collection + " WHERE key='" + key + "'");
    }

    @Override
    public Document byId(String collection, String key) {
        this.checkTable(collection);
        return executeQuery("SELECT document FROM " + collection + " WHERE key LIKE '" + key + "'", resultSet -> {
            String json = resultSet.getString("document");
            return json == null ? null : DocumentFactory.newJsonDocument(json);
        }, DocumentFactory.newJsonDocument());
    }

    @Override
    public Collection<Document> filter(String collection, String fieldName, Object fieldValue) {
        this.checkTable(collection);
        return documents(collection).stream().filter(d -> d.getObject(fieldName).equals(fieldName)).collect(Collectors.toList());
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
        return this.filter(collection, (s, document) -> true);
    }

    @Override
    public Map<String, Document> filter(String collection, BiPredicate<String, Document> predicate) {
        this.checkTable(collection);
        return executeQuery("SELECT * FROM " + collection, resultSet -> {
            Map<String, Document> documents = new HashMap<>();
            while (resultSet.next()) {
                String key = resultSet.getString("key");
                String json = resultSet.getString("document");
                Document document = DocumentFactory.newJsonDocument(json);
                if (predicate.test(key, document)) {
                    documents.put(key, document);
                }
            }
            return documents;
        }, new HashMap<>());
    }

    @Override
    public void iterate(String collection, BiConsumer<String, Document> consumer) {
        this.entries(collection).forEach(consumer);
    }

    @Override
    public void clear(String collection) {

    }

    private <T> T executeQuery(String query, DatabaseFunction<ResultSet, T> function, T defaultValue) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return function.apply(resultSet);
            } catch (Exception throwable) {
                return defaultValue;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return defaultValue;
    }

    private void executeUpdate(String query) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface DatabaseFunction<I, O> {

        O apply(I i) throws SQLException;
    }
}
