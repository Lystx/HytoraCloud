package cloud.hytora.database.api.elements;

import cloud.hytora.common.task.Task;
import cloud.hytora.database.api.IPayLoad;

import java.util.Collection;
import java.util.function.Consumer;

public interface DatabaseCollection {

    String getName();

    Collection<String> getIdentifiers();
    Task<Collection<String>> getIdentifiersAsync();

    Collection<DatabaseEntry> findEntries();

    Task<Collection<DatabaseEntry>> findEntriesAsync();

    Task<Boolean> hasEntryAsync(String identifier);

    Boolean hasEntry(String identifier);

    Task<DatabaseEntry> findEntryAsync(String identification);
    DatabaseEntry findEntry(String identification);


    Task<Collection<DatabaseEntry>> filterEntriesAsync(DatabaseFilter filter, Object... input);

    IPayLoad deleteEntry(String identifier);
    Task<IPayLoad> deleteEntryAsync(String identifier);


    IPayLoad insertEntry(Consumer<DatabaseEntry> entry);
    IPayLoad insertEntry(DatabaseEntry entry);
    Task<IPayLoad> insertEntryAsync(Consumer<DatabaseEntry> entry);
    Task<IPayLoad> insertEntryAsync(DatabaseEntry entry);

    IPayLoad updateEntry(String identification, Consumer<DatabaseEntry> entry);
    IPayLoad updateEntry(String identification, DatabaseEntry entry);
    Task<IPayLoad> updateEntryAsync(String identification, Consumer<DatabaseEntry> entry);
    Task<IPayLoad> updateEntryAsync(String identification, DatabaseEntry entry);

    IPayLoad upsertEntry(Consumer<DatabaseEntry> entry);
    IPayLoad upsertEntry(DatabaseEntry entry);
    Task<IPayLoad> upsertEntryAsync(Consumer<DatabaseEntry> entry);
    Task<IPayLoad> upsertEntryAsync(DatabaseEntry entry);

}
