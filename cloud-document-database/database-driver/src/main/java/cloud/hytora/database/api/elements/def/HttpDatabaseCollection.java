package cloud.hytora.database.api.elements.def;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.RandomString;
import cloud.hytora.common.task.Task;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.api.elements.DatabaseCollection;
import cloud.hytora.database.api.elements.DatabaseEntry;
import cloud.hytora.database.api.elements.DatabaseFilter;
import cloud.hytora.database.http.HttpDriver;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.http.api.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class HttpDatabaseCollection implements DatabaseCollection {

    private final String name;
    private final String databaseName;

    @Override
    public Collection<String> getIdentifiers() {
        return HttpDriver.getInstance()
                .sendRequest("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "identifiers");
                    param.put("database", databaseName);
                    param.put("collection", name);
                }).toBundle().map(IEntry::toString);
    }

    @Override
    public Task<Collection<String>> getIdentifiersAsync() {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "identifiers");
                    param.put("database", databaseName);
                    param.put("collection", name);
                }).map(p -> p.toBundle().map(IEntry::toString));
    }

    @Override
    public Collection<DatabaseEntry> findEntries() {
        return getIdentifiers().stream().map(this::findEntry).collect(Collectors.toList());
    }

    @Override
    public Task<Collection<DatabaseEntry>> findEntriesAsync() {
        Task<Collection<DatabaseEntry>> promise = Task.empty();
        promise.setAcceptSingleValue();
        AtomicBoolean set = new AtomicBoolean(false);
        getIdentifiersAsync()
                .onTaskSucess(identifiers -> {
                    Collection<String> ids = CollectionUtils.clearDuplicates(identifiers);
                    AtomicInteger count = new AtomicInteger(ids.size());
                    Collection<DatabaseEntry> entries = new ArrayList<>();
                    for (String identifier : ids) {
                        count.set((count.get() - 1));
                        findEntryAsync(identifier).onTaskSucess(e -> {
                            entries.add(e);
                            if (count.get() <= 0 && !set.get()) {
                                promise.setResult(entries);
                                set.set(true);
                            }
                        });
                    }
                }).onTaskFailed(promise::setFailure);
        return promise;
    }

    @Override
    public Task<Boolean> hasEntryAsync(String identifier) {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "has");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identifier);
                }).map(p -> Boolean.valueOf(p.getResponse()));
    }

    @Override
    public Boolean hasEntry(String identifier) {
        return Boolean.valueOf(
                HttpDriver.getInstance()
                        .sendRequest("query/get", HttpMethod.GET, param -> {
                            param.put("operation", "has");
                            param.put("database", databaseName);
                            param.put("collection", name);
                            param.put("identifier", identifier);
                        }).getResponse()
        );
    }

    @Override
    public Task<DatabaseEntry> findEntryAsync(String identification) {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "findSimple");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identification);
                }).map(p -> {
                    Document document = p.toDocument();
                    String id = document.removeAndGet("_id").toString();

                    return new DefaultDatabaseEntry(document, id);
                });
    }

    @Override
    public DatabaseEntry findEntry(String identification) {

        IPayLoad p = HttpDriver.getInstance()
                .sendRequest("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "findSimple");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identification);
                });
        Document document = p.toDocument();
        String id = document.removeAndGet("_id").toString();

        return new DefaultDatabaseEntry(document, id);
    }

    @Override
    public Task<Collection<DatabaseEntry>> filterEntriesAsync(DatabaseFilter filter, Object... input) {
        Task<Collection<DatabaseEntry>> promise = Task.empty();
        promise.setAcceptSingleValue();

        findEntriesAsync().onTaskSucess(databaseEntries -> {
            List<DatabaseEntry> collect = databaseEntries.stream()
                    .filter(e -> filter.checkFilter(e, input))
                    .collect(Collectors.toList());
            promise.setResult(collect);
        }).onTaskFailed(promise::setFailure);

        return promise;
    }

    @Override
    public IPayLoad deleteEntry(String identifier) {
        return HttpDriver.getInstance()
                .sendRequest("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "delete");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identifier);
                });
    }

    @Override
    public Task<IPayLoad> deleteEntryAsync(String identifier) {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "delete");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identifier);
                });
    }

    @Override
    public IPayLoad insertEntry(DatabaseEntry entry) {
        return HttpDriver.getInstance()
                .sendRequest("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "insert");
                    param.put("database", databaseName);
                    param.put("identifier", entry.getId());
                    param.put("collection", name);
                }, entry);
    }

    @Override
    public Task<IPayLoad> insertEntryAsync(DatabaseEntry entry) {
        Task<IPayLoad> promise = Task.empty();
        promise.setAcceptSingleValue();
        HttpDriver.getInstance()
                .sendRequestAsync("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "insert");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", entry.getId());
                }, entry)
                .onTaskSucess(promise::setResult)
                .onTaskFailed(promise::setFailure);
        return promise;
    }

    @Override
    public IPayLoad insertEntry(Consumer<DatabaseEntry> entry) {
        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);

        return insertEntry(dbEntry);
    }

    @Override
    public Task<IPayLoad> insertEntryAsync(Consumer<DatabaseEntry> entry) {
        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);

        return insertEntryAsync(dbEntry);
    }

    @Override
    public IPayLoad updateEntry(String identification, Consumer<DatabaseEntry> entry) {
        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);

        return this.updateEntry(identification, dbEntry);
    }


    @Override
    public IPayLoad updateEntry(String identification, DatabaseEntry entry) {
        return HttpDriver.getInstance()
                .sendRequest("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "update");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identification);
                }, entry);
    }

    @Override
    public Task<IPayLoad> updateEntryAsync(String identification, DatabaseEntry entry) {
        Task<IPayLoad> promise = Task.empty();
        promise.setAcceptSingleValue();
        HttpDriver.getInstance()
                .sendRequestAsync("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "update");
                    param.put("database", databaseName);
                    param.put("collection", name);
                    param.put("identifier", identification);
                }, entry)
                .onTaskSucess(promise::setResult)
                .onTaskFailed(promise::setFailure);
        return promise;
    }

    @Override
    public Task<IPayLoad> updateEntryAsync(String identification, Consumer<DatabaseEntry> entry) {
        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);
        return this.updateEntryAsync(identification, dbEntry);
    }

    @Override
    public IPayLoad upsertEntry(Consumer<DatabaseEntry> entry) {

        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);

        return this.upsertEntry(dbEntry);
    }

    @Override
    public IPayLoad upsertEntry(DatabaseEntry entry) {
        if (hasEntry(entry.getId())) {
            return updateEntry(entry.getId(), entry);
        } else {
            return insertEntry(entry);
        }
    }

    @Override
    public Task<IPayLoad> upsertEntryAsync(DatabaseEntry entry) {
        Task<IPayLoad> promise = Task.empty();
        promise.setAcceptSingleValue();
        hasEntryAsync(entry.getId()).onTaskSucess(b -> {
            if (b) {
                updateEntryAsync(entry.getId(), entry)
                        .onTaskSucess(promise::setResult)
                        .onTaskFailed(promise::setFailure);
            } else {
                insertEntryAsync(entry)
                        .onTaskSucess(promise::setResult)
                        .onTaskFailed(promise::setFailure);
            }
        });

        return promise;
    }

    @Override
    public Task<IPayLoad> upsertEntryAsync(Consumer<DatabaseEntry> entry) {

        DatabaseEntry dbEntry = new DefaultDatabaseEntry(new RandomString(10).nextString());
        entry.accept(dbEntry);

        return upsertEntryAsync(dbEntry);
    }
}
