package cloud.hytora.node.impl.database.impl.mongo;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.node.impl.database.IDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DatabaseMongoImpl implements IDatabase {
    @Override
    public void connect() {

    }

    @Override
    public @NotNull Wrapper<Boolean> disconnect() {
        return null;
    }

    @Override
    public List<ServerConfiguration> getAllServiceGroups() {
        return null;
    }

    @Override
    public void addGroup(@NotNull ServerConfiguration serviceGroup) {

    }

    @Override
    public void removeGroup(@NotNull ServerConfiguration serviceGroup) {

    }
}
