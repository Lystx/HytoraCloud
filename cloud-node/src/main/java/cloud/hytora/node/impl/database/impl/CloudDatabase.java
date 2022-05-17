package cloud.hytora.node.impl.database.impl;


import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.SimpleServerConfiguration;
import cloud.hytora.driver.services.configuration.bundle.ConfigurationParent;
import cloud.hytora.driver.services.configuration.bundle.SimpleParent;
import cloud.hytora.node.impl.database.IDatabase;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CloudDatabase {

    /**
     * The wrapped database
     */
    private final IDatabase database;

    private final String configurationCollection = "configurations";
    private final String parentCollection = "groups";

    public void saveConfiguration(ServerConfiguration con) {
        this.database.insert(configurationCollection, con.getName(), DocumentFactory.newJsonDocument(con));
    }

    public void saveParentConfiguration(ConfigurationParent parent) {
        this.database.insert(parentCollection, parent.getName(), DocumentFactory.newJsonDocument(parent));
    }

    public void deleteConfiguration(ServerConfiguration con) {
        this.database.delete(configurationCollection, con.getName());
    }

    public void deleteParent(ConfigurationParent parent) {
        this.database.delete(parentCollection, parent.getName());
    }

    public Collection<ConfigurationParent> getAllParentConfigurations() {
        return this.database.documents(parentCollection).stream().map(d -> d.toInstance(SimpleParent.class)).collect(Collectors.toList());
    }

    public Collection<ServerConfiguration> getAllConfigurations() {
        return this.database.documents(configurationCollection).stream().map(d -> d.toInstance(SimpleServerConfiguration.class)).collect(Collectors.toList());
    }
}
