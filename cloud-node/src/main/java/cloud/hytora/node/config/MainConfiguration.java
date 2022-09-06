package cloud.hytora.node.config;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.node.config.ServiceCrashPrevention;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.uuid.PlayerLoginProcessing;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.database.config.DatabaseConfiguration;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;


@Getter
@AllArgsConstructor
@Setter
public class MainConfiguration {

    private LogLevel logLevel;
    private ServiceProcessType serviceProcessType;
    private PlayerLoginProcessing playerLoginProcessing;
    private int proxyStartPort, spigotStartPort;
    private boolean uniqueIdCaching;
    private Collection<String> whitelistedPlayers;

    private ProtocolAddress[] httpListeners;
    private ServiceCrashPrevention serviceCrashPrevention;
    private Collection<JavaVersion> javaVersions;

    private DatabaseConfiguration databaseConfiguration;
    private DefaultNodeConfig nodeConfig;
    private CloudMessages messages;

    public Collection<JavaVersion> getJavaVersions() {
        return new ArrayList<>(javaVersions);
    }

    public static MainConfiguration getInstance() {
        return NodeDriver.getInstance().getConfigManager().getConfig();
    }

}
