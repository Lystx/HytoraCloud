package cloud.hytora.driver.config;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.entity.services.utils.ServiceProcessType;

import java.util.Collection;
import java.util.UUID;

/**
 * The {@link INetworkConfig} contains most of the important values for developing use.
 *
 *
 */
public interface INetworkConfig extends IConfig {

    ISpigotConfig getSpigotConfig();
    IServiceCrashPrevention getServiceCrashPrevention();
    LogLevel getLogLevel();
    ServiceProcessType getServiceProcessType();
    Collection<String> getWhitelistedPlayers();
    ProtocolAddress[] getHttpListeners();
    Collection<IJavaVersion> getJavaVersions();
    UniversalCloudMessages getMessages();
    int getProxyStartPort();
    int getSpigotStartPort();
    UUID getUniqueNetworkId();
    boolean isKickPlayersNotOnFallback();

    void setSpigotConfig(ISpigotConfig config);
    void setServiceCrashPrevention(IServiceCrashPrevention config);
    void setLogLevel(LogLevel logLevel);
    void setServiceProcessType(ServiceProcessType type);
    void setWhitelistedPlayers(Collection<String> players);
    void setHttpListeners(ProtocolAddress[] httpListeners);
    void setJavaVersions(Collection<IJavaVersion> javaVersions);
    void setMessages(UniversalCloudMessages messages);
    void setProxyStartPort(int port);
    void setSpigotStartPort(int port);
    void setKickPlayersNotOnFallback(boolean value);


}
