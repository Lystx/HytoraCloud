package cloud.hytora.node.impl.config;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.http.SSLConfiguration;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.node.config.SimpleJavaVersion;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import cloud.hytora.node.impl.database.config.DatabaseType;
import lombok.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                            8876,
                            false,
                            2,
                            new ServiceCrashPrevention(
                                    true,
                                    10,
                                    TimeUnit.SECONDS
                            ),
                            Collections.singleton(new SimpleJavaVersion("JAVA_16", "your/path/to/java/version/", 16)),
                            new ProtocolAddress[0],
                            new ProtocolAddress[]{new ProtocolAddress("127.0.0.1", 4518)},
                            new SSLConfiguration(
                                    false,
                                    false,
                                    "/etc/ssl/certificate.pem",
                                    "/etc/ssl/privateKey.key"
                            )
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
