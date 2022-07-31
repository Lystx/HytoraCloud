package cloud.hytora.driver.database;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.common.IdentityObject;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class DatabaseSection<T extends IBufferObject> {

    /**
     * The database
     */
    private final IDatabase database;

    /**
     * The name of collection
     */
    private final String collectionName;

    /**
     * The generic wrapper class type
     */
    private final Class<T> typeClass;

    public void insert(String key, T value) {
        database.insert(collectionName, key, DocumentFactory.newJsonDocument(value));
    }

    public <E extends IdentityObject> void insert(E value) {
        database.insert(collectionName, value.getMainIdentity(), DocumentFactory.newJsonDocument(value));
    }

    public void update(String key, T value) {
        this.database.update(collectionName, key, DocumentFactory.newJsonDocument(value));
    }

    public void upsert(String key, T value) {
        if (this.database.contains(collectionName, key)) {
            this.update(key, value);
        } else {
            this.insert(key, value);
        }
    }

    public <E extends IdentityObject> void upsert(E value) {
        this.upsert(value.getMainIdentity(), (T) value);
    }


    public void delete(String key) {
        database.delete(collectionName, key);
    }

    public T findById(String key) {
        Document document = database.byId(collectionName, key);
        return (document == null || document.isEmpty()) ? null :  document.toInstance(this.typeClass);
    }

    public T findByMatch(String key, Object value) {
        Document document = database.filter(collectionName, key, value).stream().findFirst().orElse(null);
        return (document == null || document.isEmpty()) ? null : document.toInstance(typeClass);
    }

    public Collection<T> getAll() {
        return this.database.documents(collectionName).stream().map(d -> (d == null || d.isEmpty()) ? null : d.toInstance(typeClass)).collect(Collectors.toList());
    }

}
