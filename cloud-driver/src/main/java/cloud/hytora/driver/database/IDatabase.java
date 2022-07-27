package cloud.hytora.driver.database;

import cloud.hytora.document.Document;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface IDatabase {

    void connect();

    void disconnect();

    void insert(String collection, String key, Document document);

    void update(String collection,  String key, Document document);

    boolean contains(String collection,  String key);

    void delete(String collection,  String key);

    Document byId(String collection, String key);

    Collection<Document> filter(String collection, String fieldName, Object fieldValue);

    Collection<String> keys(String collection);

    Collection<Document> documents(String collection);

    Map<String, Document> entries(String collection);

    Map<String, Document> filter(String collection, BiPredicate<String, Document> predicate);

    void iterate(String collection, BiConsumer<String, Document> consumer);

    void clear(String collection);


}
