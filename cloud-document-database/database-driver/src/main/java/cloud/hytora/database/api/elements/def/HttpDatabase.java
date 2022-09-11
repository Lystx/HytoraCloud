package cloud.hytora.database.api.elements.def;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.api.elements.Database;
import cloud.hytora.database.api.elements.DatabaseCollection;
import cloud.hytora.database.api.exceptions.NoSuchCollectionException;
import cloud.hytora.database.http.HttpDriver;
import cloud.hytora.document.IEntry;
import cloud.hytora.http.api.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@Getter
public class HttpDatabase implements Database {

    private String name;

    @Override
    public IPromise<Collection<DatabaseCollection>> getCollectionsAsync() {
        IPromise<Collection<DatabaseCollection>> promise = IPromise.empty();
        HttpDriver.getInstance()
                .sendRequestAsync("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "listCollections");
                    param.put("database", name);
                }).onTaskSucess(p -> {
                    Collection<DatabaseCollection> collection = new ArrayList<>();
                    for (IEntry iEntry : p.toBundle()) {
                        System.out.println(iEntry.toString());
                        collection.add(getCollection(iEntry.toString()));
                    }
                    promise.setResult(collection);
                });
        return promise;
    }

    @Override
    public Collection<DatabaseCollection> getCollections() {
        return HttpDriver.getInstance()
                .sendRequest("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "listCollections");
                    param.put("database", name);
                }).toBundle().map(e -> getCollection(e.toString()));
    }

    @Override
    public boolean hasCollection(String name) {
        return Boolean.parseBoolean(
                HttpDriver.getInstance()
                        .sendRequest("query/get", HttpMethod.GET, param -> {
                            param.put("operation", "existsCollection");
                            param.put("collection", name);
                            param.put("database", this.name);
                        }).getResponse()
        );
    }

    @Override
    public DatabaseCollection createCollection(String name) {
        HttpDriver.getInstance()
                .sendRequest("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "createCollection");
                    param.put("collection", name);
                    param.put("database", this.name);
                });
        return rawCollection(name);
    }

    @Override
    public DatabaseCollection rawCollection(String name) {
        return new HttpDatabaseCollection(name, this.name);
    }

    @Override
    public IPromise<DatabaseCollection> createCollectionAsync(String name) {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "createCollection");
                    param.put("collection", name);
                    param.put("database", this.name);
                }).map(p -> rawCollection(name));
    }

    @Override
    public IPromise<Boolean> hasCollectionAsync(String name) {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/get", HttpMethod.GET, param -> {
                    param.put("operation", "existsCollection");
                    param.put("collection", name);
                    param.put("database", this.name);
                }).map(p -> Boolean.valueOf(p.getResponse()));
    }

    @Override
    public IPromise<IPayLoad> dropAsync() {
        return HttpDriver.getInstance()
                .sendRequestAsync("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "drop");
                    param.put("collection", name);
                    param.put("database", this.name);
                });
    }

    @Override
    public IPayLoad drop() {
        return HttpDriver.getInstance()
                .sendRequest("query/post", HttpMethod.POST, param -> {
                    param.put("operation", "drop");
                    param.put("collection", name);
                    param.put("database", this.name);
                });
    }

    @Override
    public DatabaseCollection getCollection(String name) {
        if (!hasCollection(name)) {
            throw new NoSuchCollectionException();
        }
        return new HttpDatabaseCollection(name, this.name);
    }

    @Override
    public IPromise<DatabaseCollection> getCollectionAsync(String name) {
        IPromise<DatabaseCollection> promise = IPromise.empty();
        hasCollectionAsync(name).onTaskSucess(b -> {
            if (b) {
                promise.setResult(new HttpDatabaseCollection(name, this.name));
            } else {
                promise.setFailure(new NoSuchCollectionException());
            }
        });
        return promise;
    }
}
