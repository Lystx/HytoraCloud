package cloud.hytora.node.impl.config;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.node.config.SimpleJavaVersion;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.DatabaseConfiguration;
import cloud.hytora.node.impl.database.DatabaseType;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Getter @Setter
public class ConfigManager {

    private boolean didExist;
    private MainConfiguration config;

    public void read() throws IOException {
        if (NodeDriver.CONFIG_FILE.exists()) {
            this.didExist = true;
            this.config = DocumentFactory.newJsonDocument(NodeDriver.CONFIG_FILE).toInstance(MainConfiguration.class);
        } else {
            this.didExist = false;
            this.config = new MainConfiguration(
                    new DatabaseConfiguration(
                            DatabaseType.FILE,
                            "127.0.0.1",
                            3306,
                            "cloud",
                            "",
                            "cloud",
                            "password123"
                    ),
                    new DefaultNodeConfig(
                            "Node-1",
                            UUID.randomUUID().toString(),
                            "127.0.0.1",
                            new ProtocolAddress[0],
                            8876,
                            false,
                            Collections.singleton(new SimpleJavaVersion("JAVA_16", "your/path/to/java/version/", 16))
                    ),25565, 30000, new ArrayList<>());
        }

        if (this.config.getNodeConfig().getClusterAddresses().length > 0) {
            this.config.getNodeConfig().markAsRemote();
        }
    }

    public void save() throws IOException {
        DocumentFactory.newJsonDocument(this.config).saveToFile(NodeDriver.CONFIG_FILE);
    }
}
