package cloud.hytora.driver.database;

import cloud.hytora.document.Document;
import cloud.hytora.driver.common.objects.CloudJsonEntity;
import cloud.hytora.driver.common.objects.Identifiable;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.types.BufferState;
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
        if (value instanceof CloudJsonEntity) {
            CloudJsonEntity documentable = (CloudJsonEntity) value;
            document = Document.gson();
            documentable.handleJsonOperation(BufferState.WRITE, document);
        } else {
            document = Document.json(value);
        }
        storage.insert(collectionName, key, document);
    }

    public <E extends Identifiable> void insert(E value) {
        Document document;
        if (value instanceof CloudJsonEntity) {
            CloudJsonEntity documentable = (CloudJsonEntity) value;
            document = Document.gson();
            documentable.handleJsonOperation(BufferState.WRITE, document);
        } else {
            document = Document.json(value);
        }
        storage.insert(collectionName, value.getMainIdentity(), document);
    }

    public void update(String key, T value) {
        Document document;
        if (value instanceof CloudJsonEntity) {
            CloudJsonEntity documentable = (CloudJsonEntity) value;
            document = Document.gson();
            documentable.handleJsonOperation(BufferState.WRITE, document);
        } else {
            document = Document.json(value);
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

    public <E extends Identifiable> void upsert(E value) {
        this.upsert(value.getMainIdentity(), (T) value);
    }


    public void delete(String key) {
        storage.delete(collectionName, key);
    }

    public T findById(String key) {
        Document document = storage.byId(collectionName, key);
        if (document != null) {

            if (CloudJsonEntity.class.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(CloudJsonEntity.class)) {
                try {
                    CloudJsonEntity t = (CloudJsonEntity) typeClass.newInstance();
                    t.handleJsonOperation(BufferState.READ, document);
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
            if (CloudJsonEntity.class.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(CloudJsonEntity.class)) {
                try {
                    CloudJsonEntity t = (CloudJsonEntity) typeClass.newInstance();
                    t.handleJsonOperation(BufferState.READ, document);
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
