package cloud.hytora.database.api.elements;

import cloud.hytora.common.task.Task;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.api.exceptions.NoSuchCollectionException;

import java.util.Collection;

public interface Database {

    String getName();

    Task<Collection<DatabaseCollection>> getCollectionsAsync();

    Collection<DatabaseCollection> getCollections();


    default DatabaseCollection getCollectionOrCreate(String name) {
        if (hasCollection(name)) {
            return rawCollection(name);
        } else {
            return createCollection(name);
        }
    }

    default Task<DatabaseCollection> getCollectionOrCreateAsync(String name) {
        Task<DatabaseCollection> promise = Task.empty();
        hasCollectionAsync(name)
                .onTaskFailed(promise::setFailure)
                .onTaskSucess(b -> {
                    if (b) {
                        getCollectionAsync(name).onTaskSucess(promise::setResult);
                    } else {
                        createCollectionAsync(name).onTaskSucess(promise::setResult);
                    }
                });
        return promise;
    }

    DatabaseCollection createCollection(String name);

    Task<DatabaseCollection> createCollectionAsync(String name);

    DatabaseCollection getCollection(String name) throws NoSuchCollectionException;

    @Deprecated
    DatabaseCollection rawCollection(String name);

    Task<DatabaseCollection> getCollectionAsync(String name);

    boolean hasCollection(String name);

    Task<Boolean> hasCollectionAsync(String name);

    Task<IPayLoad> dropAsync();

    IPayLoad drop();

}
