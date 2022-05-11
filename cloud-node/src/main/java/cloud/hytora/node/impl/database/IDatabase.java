package cloud.hytora.node.impl.database;


import cloud.hytora.common.wrapper.Wrapper;

import cloud.hytora.driver.services.configuration.ServerConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDatabase {

    void connect();

    @NotNull
    Wrapper<Boolean> disconnect();

    List<ServerConfiguration> getAllServiceGroups();

    void addGroup(@NotNull ServerConfiguration serviceGroup);

    void removeGroup(@NotNull ServerConfiguration serviceGroup);


}
