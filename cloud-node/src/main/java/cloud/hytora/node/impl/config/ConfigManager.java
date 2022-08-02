package cloud.hytora.node.impl.config;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.RandomString;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.uuid.PlayerLoginProcessing;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import cloud.hytora.node.impl.database.config.DatabaseType;
import lombok.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ConfigManager {

    private boolean didExist;
    private MainConfiguration config;


    public void read() throws IOException {
        Logger.constantInstance().trace("Reading config.json (NodeConfiguration)...");
        if (NodeDriver.CONFIG_FILE.exists()) {
            Logger.constantInstance().trace("Config-File does exist ==> Reading existing config...");
            this.didExist = true;
            this.config = DocumentFactory.newJsonDocument(NodeDriver.CONFIG_FILE).toInstance(MainConfiguration.class);
        } else {
            this.didExist = false;
            this.config = new MainConfiguration(
                    LogLevel.INFO,
                    ServiceProcessType.BRIDGE_PLUGIN,
                    PlayerLoginProcessing.UUID_CACHE,
                    25565,
                    40000,
                    true,
                    Collections.singletonList("Notch"),
                    new ProtocolAddress[]{new ProtocolAddress("127.0.0.1", 4518)},
                    new ServiceCrashPrevention(
                            true,
                            10,
                            TimeUnit.SECONDS
                    ),
                    new ArrayList<>(),
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
                            UUID.randomUUID(),
                            new ProtocolAddress("127.0.0.1", 8876),
                            new RandomString(10).nextString(),
                            false,
                            2,
                            10000L,
                            new ProtocolAddress[0]
                    ), new CloudMessages());
            Logger.constantInstance().trace("Config-File does not exist ==> Creating and saving default config..");
        }

        if (this.config.getNodeConfig().getClusterAddresses().length > 0) {
            this.config.getNodeConfig().setRemote();
        }
        Logger.constantInstance().trace("Config loaded successfully!");
    }

    public void save() throws IOException {
        Logger.constantInstance().trace("Current Configuration was saved in config.json!");
        DocumentFactory.newJsonDocument(this.config).saveToFile(NodeDriver.CONFIG_FILE);
    }
}
