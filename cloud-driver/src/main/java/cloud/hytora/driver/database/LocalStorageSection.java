package cloud.hytora.driver.database;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.common.Documentable;
import cloud.hytora.driver.common.IdentityObject;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class LocalStorageSection<T extends IBufferObject> {

    /**
     * The database
     */
    private final IJsonStorage storage;

    /**
     * The name of collection
     */
    private final String collectionName;

    /**
     * The generic wrapper class type
     */
    private final Class<T> typeClass;

    public void insert(String key, T value) {
        Document document;
        if (value instanceof Documentable) {
            Documentable<?> documentable = (Documentable<?>) value;
            document = documentable.toDocument();
        } else {
            document = DocumentFactory.newJsonDocument(value);
        }
        storage.insert(collectionName, key, document);
    }

    public <E extends IdentityObject> void insert(E value) {
        Document document;
        if (value instanceof Documentable) {
            Documentable<?> documentable = (Documentable<?>) value;
            document = documentable.toDocument();
        } else {
            document = DocumentFactory.newJsonDocument(value);
        }
        storage.insert(collectionName, value.getMainIdentity(), document);
    }

    public void update(String key, T value) {
        Document document;
        if (value instanceof Documentable) {
            Documentable<?> documentable = (Documentable<?>) value;
            document = documentable.toDocument();
        } else {
            document = DocumentFactory.newJsonDocument(value);
        }
        this.storage.update(collectionName, key, document);
    }

    public void upsert(String key, T value) {
        if (this.storage.contains(collectionName, key)) {
            this.update(key, value);
        } else {
            this.insert(key, value);
        }
    }

    public <E extends IdentityObject> void upsert(E value) {
        this.upsert(value.getMainIdentity(), (T) value);
    }


    public void delete(String key) {
        storage.delete(collectionName, key);
    }

    public T findById(String key) {
        Document document = storage.byId(collectionName, key);
        if (document != null) {

            if (Documentable.class.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(Documentable.class)) {
                try {
                    Documentable<T> t = (Documentable<T>) typeClass.newInstance();
                    t.applyDocument(document);
                    return (T) t;
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Returned null for DB-Match (Key: " + key + ")");
        }
        return (document == null || document.isEmpty()) ? null :  document.toInstance(this.typeClass);
    }

    public T findByMatch(String key, Object value) {
        Document document = storage.filter(collectionName, key, value).stream().findFirst().orElse(null);
        if (document != null) {
            if (Documentable.class.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(Documentable.class)) {
                try {
                    Documentable<T> t = (Documentable<T>) typeClass.newInstance();
                    t.applyDocument(document);
                    return (T) t;
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Returned null for DB-Match (Key: " + key + " | Value: " + value + ")");
        }
        return (document == null || document.isEmpty()) ? null : document.toInstance(typeClass);
    }

    public Collection<T> getAll() {
        return this.storage.documents(collectionName).stream().map(d -> (d == null || d.isEmpty()) ? null : d.toInstance(typeClass)).collect(Collectors.toList());
    }

}
