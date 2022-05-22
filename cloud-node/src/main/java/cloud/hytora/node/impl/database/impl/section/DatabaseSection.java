package cloud.hytora.node.impl.database.impl.section;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.common.IdentityHolder;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.node.impl.database.IDatabase;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DatabaseSection<T extends Bufferable> {

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

    public <E extends IdentityHolder> void insert(E value) {
        database.insert(collectionName, value.getMainIdentity(), DocumentFactory.newJsonDocument(value));
    }

    public void update(String key, T value) {
        this.database.update(collectionName, key, DocumentFactory.newJsonDocument(value));
    }

    public void delete(String key) {
        database.delete(collectionName, key);
    }

    public <E> E get(String key) {
        return (E) database.byId(collectionName, key).toInstance(this.typeClass);
    }

    public Collection<T> getAll() {
        return this.database.documents(collectionName).stream().map(d -> d.toInstance(typeClass)).collect(Collectors.toList());
    }

}
