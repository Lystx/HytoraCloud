package cloud.hytora.node.impl.config;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.config.DatabaseConfiguration;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;


@Getter
@AllArgsConstructor
@Setter
public class MainConfiguration {

    private LogLevel logLevel;
    private DatabaseConfiguration databaseConfiguration;
    private DefaultNodeConfig nodeConfig;

    private int proxyStartPort;
    private int spigotStartPort;
    private java.util.List<String> whitelistedPlayers;

    public static MainConfiguration getInstance() {
        return NodeDriver.getInstance().getConfigManager().getConfig();
    }

}
