package cloud.hytora.node.impl.database.impl.sql;

import cloud.hytora.common.wrapper.Wrapper;
import com.google.common.collect.Lists;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.impl.database.DatabaseConfiguration;
import cloud.hytora.node.impl.database.IDatabase;
import cloud.hytora.node.impl.database.impl.DatabaseFunction;


import lombok.SneakyThrows;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.List;
import java.util.Objects;

public class DatabaseSqlImpl implements IDatabase {

    private Connection connection;

    // TODO: 26.04.2022 fix mysql with table columns
    private String tableName = "cloudsystem_groups";

    @SneakyThrows
    @Override
    public void connect() {
        DatabaseConfiguration databaseConfiguration = MainConfiguration.getInstance().getDatabaseConfiguration();
        this.connection = DriverManager.getConnection("jdbc:mysql://" + databaseConfiguration.getHost() + ":" + databaseConfiguration.getPort()
                        + "/" + databaseConfiguration.getDatabase() + "?useUnicode=true&autoReconnect=true",
                databaseConfiguration.getUser(), databaseConfiguration.getPassword());

        executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(name VARCHAR(100), template VARCHAR(100), node VARCHAR(100)," +
                " memory INT, minOnlineService INT, maxOnlineService INT, staticService INT, fallbackGroup INT, version VARCHAR(100), maxPlayers INT, motd TEXT)");

        CloudDriver.getInstance().getLogger().info("The connection is now established to the database.");
    }

    @SneakyThrows
    @Override
    public @NotNull Wrapper<Boolean> disconnect() {
        Wrapper<Boolean> shutdownPromise = Wrapper.empty();
        if (this.connection != null) {
            this.connection.close();
        }
        shutdownPromise.setResult(true);
        return shutdownPromise;
    }

    @Override
    public void addGroup(@NotNull ServerConfiguration serviceGroup) {
        executeUpdate("INSERT INTO " + tableName + "(name, template, node, memory, minOnlineService, maxOnlineService, staticService, fallbackGroup, version, maxPlayers, motd) VALUES (" +
                "'" + serviceGroup.getName() + "', '" + serviceGroup.getTemplate() + "', '" + serviceGroup.getNode() + "', " + serviceGroup.getMemory() + ", " +
                serviceGroup.getMinOnlineService() + ", " + serviceGroup.getMaxOnlineService() + ", " + (serviceGroup.getShutdownBehaviour() == ServiceShutdownBehaviour.KEEP ? 1 : 0) +
                ", " + (serviceGroup.getFallback().isEnabled() ? 1 : 0) + ", '" + serviceGroup.getVersion().name() + "', " + serviceGroup.getDefaultMaxPlayers() +
                ",'" + serviceGroup.getMotd() + "');");
    }

    @Override
    public void removeGroup(@NotNull ServerConfiguration serviceGroup) {
        executeUpdate("DELETE FROM " + tableName + " WHERE name='" + serviceGroup.getName() + "'");
    }

    @Override
    public List<ServerConfiguration> getAllServiceGroups() {
        return executeQuery("SELECT * FROM " + tableName, resultSet -> {
            List<ServerConfiguration> groups = Lists.newArrayList();
            /*while (resultSet.next()) {
                SimpleServiceConfiguration serviceGroup = new SimpleServiceConfiguration(
                        resultSet.getString("name"),
                        resultSet.getString("template"),
                        resultSet.getString("node"),
                        resultSet.getString("motd"),
                        resultSet.getInt("memory"),
                        resultSet.getInt("maxPlayers"),
                        resultSet.getInt("minOnlineService"),
                        resultSet.getInt("maxOnlineService"),
                        resultSet.getInt("fallbackGroup") == 1,
                        ServiceVersion.getVersionByTitle(resultSet.getString("version")),
                        ServiceShutdownBehaviour.valueOf(resultSet.getString("behaviour"))
                );
                groups.add(serviceGroup);
            }*/
            return groups;
        }, Lists.newArrayList());
    }

    public <T> T executeQuery(String query, DatabaseFunction<ResultSet, T> function, T defaultValue) {
        Objects.requireNonNull(this.connection, "Try to execute a statement, but the connection is null.");
        try (var preparedStatement = this.connection.prepareStatement(query)) {
            try (var resultSet = preparedStatement.executeQuery()) {
                return function.apply(resultSet);
            } catch (Exception throwable) {
                return defaultValue;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return defaultValue;
    }

    public int executeUpdate(final String query) {
        Objects.requireNonNull(this.connection, "Try to update a statement, but the connection is null.");
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return -1;
        }
    }

}
