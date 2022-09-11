package cloud.hytora.database.api.elements;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.api.exceptions.NoSuchCollectionException;

import java.util.Collection;

public interface Database {

    String getName();

    IPromise<Collection<DatabaseCollection>> getCollectionsAsync();

    Collection<DatabaseCollection> getCollections();


    default DatabaseCollection getCollectionOrCreate(String name) {
        if (hasCollection(name)) {
            return rawCollection(name);
        } else {
            return createCollection(name);
        }
    }

    default IPromise<DatabaseCollection> getCollectionOrCreateAsync(String name) {
        IPromise<DatabaseCollection> promise = IPromise.empty();
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

    IPromise<DatabaseCollection> createCollectionAsync(String name);

    DatabaseCollection getCollection(String name) throws NoSuchCollectionException;

    @Deprecated
    DatabaseCollection rawCollection(String name);

    IPromise<DatabaseCollection> getCollectionAsync(String name);

    boolean hasCollection(String name);

    IPromise<Boolean> hasCollectionAsync(String name);

    IPromise<IPayLoad> dropAsync();

    IPayLoad drop();

}
