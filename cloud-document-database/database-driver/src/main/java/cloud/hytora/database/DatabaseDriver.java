package cloud.hytora.database;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.api.elements.Database;
import cloud.hytora.database.api.elements.def.HttpDatabase;
import cloud.hytora.database.http.HttpDriver;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.HttpMethod;
import lombok.Getter;

import java.util.Collection;

public class DatabaseDriver {

    @Getter
    private static DatabaseDriver instance;

    private final String username;

    public DatabaseDriver(String username) {
        instance = this;

        this.username = username;
    }

    public IPromise<IPayLoad> connect(HttpAddress address, String token) {
        IPromise<IPayLoad> promise = IPromise.empty();
        HttpDriver driver = new HttpDriver(token, address);

        driver.sendRequestAsync(
                        "connection/ping",
                        HttpMethod.POST,
                        Document.newJsonDocument(
                                "name", this.username,
                                "address", HttpAddress.currentPublicIp().toString()
                        )
                ).onTaskSucess(promise::setResult)
                .onTaskFailed(promise::setFailure);

        return promise;
    }

    public IPromise<IPayLoad> close() {
        IPromise<IPayLoad> promise = IPromise.empty();

        HttpDriver.getInstance().sendRequestAsync(
                        "connection/close",
                        HttpMethod.POST,
                        Document.newJsonDocument(
                                "name", this.username,
                                "address", HttpAddress.currentPublicIp().toString()
                        )
                ).onTaskSucess(promise::setResult)
                .onTaskFailed(promise::setFailure);

        return promise;
    }

    public Database getDatabase(String name) {
        return new HttpDatabase(name);
    }

    public Collection<Database> getDatabases() {
        Bundle bundle = HttpDriver
                .getInstance()
                .sendRequest("database/findAll", HttpMethod.GET)
                .toBundle();
        return bundle.map(entry -> getDatabase(entry.toString()));
    }
}
