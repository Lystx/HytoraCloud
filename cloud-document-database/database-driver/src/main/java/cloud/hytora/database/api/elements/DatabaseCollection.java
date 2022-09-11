package cloud.hytora.database.api.elements;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.database.api.IPayLoad;

import java.util.Collection;
import java.util.function.Consumer;

public interface DatabaseCollection {

    String getName();

    Collection<String> getIdentifiers();
    IPromise<Collection<String>> getIdentifiersAsync();

    Collection<DatabaseEntry> findEntries();

    IPromise<Collection<DatabaseEntry>> findEntriesAsync();

    IPromise<Boolean> hasEntryAsync(String identifier);

    Boolean hasEntry(String identifier);

    IPromise<DatabaseEntry> findEntryAsync(String identification);
    DatabaseEntry findEntry(String identification);


    IPromise<Collection<DatabaseEntry>> filterEntriesAsync(DatabaseFilter filter, Object... input);

    IPayLoad deleteEntry(String identifier);
    IPromise<IPayLoad> deleteEntryAsync(String identifier);


    IPayLoad insertEntry(Consumer<DatabaseEntry> entry);
    IPayLoad insertEntry(DatabaseEntry entry);
    IPromise<IPayLoad> insertEntryAsync(Consumer<DatabaseEntry> entry);
    IPromise<IPayLoad> insertEntryAsync(DatabaseEntry entry);

    IPayLoad updateEntry(String identification, Consumer<DatabaseEntry> entry);
    IPayLoad updateEntry(String identification, DatabaseEntry entry);
    IPromise<IPayLoad> updateEntryAsync(String identification, Consumer<DatabaseEntry> entry);
    IPromise<IPayLoad> updateEntryAsync(String identification, DatabaseEntry entry);

    IPayLoad upsertEntry(Consumer<DatabaseEntry> entry);
    IPayLoad upsertEntry(DatabaseEntry entry);
    IPromise<IPayLoad> upsertEntryAsync(Consumer<DatabaseEntry> entry);
    IPromise<IPayLoad> upsertEntryAsync(DatabaseEntry entry);

}
