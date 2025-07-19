package cloud.hytora.driver.config.def;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.misc.RandomString;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.*;
import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.networking.packets.other.PacketNetworkConfig;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.utils.ServiceProcessType;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cloud.hytora.driver.CloudDriver.Environment.NODE;


@Getter
@AllArgsConstructor
@Setter
public class UniversalNetworkConfig implements INetworkConfig {

    private UUID uniqueNetworkId;
    private boolean kickPlayersNotOnFallback;
    private LogLevel logLevel;
    private ServiceProcessType serviceProcessType;
    private int proxyStartPort, spigotStartPort;
    private Collection<String> whitelistedPlayers;

    private ProtocolAddress[] httpListeners;

    private Collection<UniversalJavaVersion> javaVersions;

    private UniversalSpigotConfig spigotConfig;
    private UniversalSCP serviceCrashPrevention;
    private UniversalDatabaseConfig databaseConfig;
    private UniversalNodeConfig nodeConfig;
    private UniversalCloudMessages messages;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.uniqueNetworkId = buf.readUniqueId();
                this.kickPlayersNotOnFallback = buf.readBoolean();
                this.logLevel = buf.readEnum(LogLevel.class);
                this.serviceProcessType = buf.readEnum(ServiceProcessType.class);

                this.proxyStartPort = buf.readInt();
                this.spigotStartPort = buf.readInt();

                this.whitelistedPlayers = buf.readStringCollection();
                this.httpListeners = buf.readObjectArray(ProtocolAddress.class);
                this.javaVersions = buf.readWrapperObjectCollection(UniversalJavaVersion.class);

                this.spigotConfig = buf.readObject(UniversalSpigotConfig.class);
                this.serviceCrashPrevention = buf.readObject(UniversalSCP.class);
                this.databaseConfig = buf.readObject(UniversalDatabaseConfig.class);
                this.nodeConfig = buf.readObject(UniversalNodeConfig.class);
                this.messages = buf.readObject(UniversalCloudMessages.class);
                break;
            case WRITE:
                buf.writeUniqueId(uniqueNetworkId);
                buf.writeBoolean(kickPlayersNotOnFallback);
                buf.writeEnum(logLevel);
                buf.writeEnum(serviceProcessType);

                buf.writeInt(proxyStartPort);
                buf.writeInt(spigotStartPort);

                buf.writeStringCollection(whitelistedPlayers);
                buf.writeObjectArray(httpListeners);
                buf.writeObjectCollection(this.javaVersions);

                buf.writeObject(spigotConfig);
                buf.writeObject(serviceCrashPrevention);
                buf.writeObject(databaseConfig);
                buf.writeObject(nodeConfig);
                buf.writeObject(messages);
                break;
        }
    }
    public static UniversalNetworkConfig getInstance() {
        return (UniversalNetworkConfig) CloudDriver.getInstance().getConfigManager().getConfig();
    }


    @Override
    public void setNodeConfig(INodeConfig config) {
        this.nodeConfig = (UniversalNodeConfig) config;
    }

    @Override
    public void setSpigotConfig(ISpigotConfig config) {
        this.spigotConfig = (UniversalSpigotConfig) config;
    }

    @Override
    public void setDatabaseConfig(IDatabaseConfig config) {
        this.databaseConfig = (UniversalDatabaseConfig) config;
    }

    @Override
    public void setServiceCrashPrevention(IServiceCrashPrevention config) {
        this.serviceCrashPrevention = (UniversalSCP) config;
    }

    @Override
    public void setJavaVersions(Collection<IJavaVersion> javaVersions) {
        this.javaVersions = new ArrayList<>();
        for (IJavaVersion javaVersion : javaVersions) {
            this.javaVersions.add((UniversalJavaVersion) javaVersion);
        }
    }

    @Override
    public Collection<IJavaVersion> getJavaVersions() {
        return new ArrayList<>(javaVersions);
    }

    @Override
    public void update() {
        if (CloudDriver.getInstance().getEnvironment() == NODE) {
            CloudDriver.getInstance().getConfigManager().save();
        }
        CloudDriver.getInstance().getConfigManager().setConfig(this);
        PacketNetworkConfig.forUpdateConfig(this, true).publish();
    }

    public static UniversalNetworkConfig getDefault() {
        return new UniversalNetworkConfig(
                UUID.randomUUID(),
                true,
                LogLevel.INFO,
                ServiceProcessType.BRIDGE_PLUGIN,
                25565,
                40000,
                new ArrayList<>(),
                new ProtocolAddress[]{new ProtocolAddress("127.0.0.1", 4518)},
                new ArrayList<>(),
                new UniversalSpigotConfig(
                        "none",
                        false,
                        true
                ),
                new UniversalSCP(
                        true,
                        10,
                        TimeUnit.SECONDS
                ),
                new UniversalDatabaseConfig(
                        DatabaseType.FILE,
                        "127.0.0.1",
                        3306,
                        "cloud",
                        "",
                        "cloud",
                        "password123"
                ),
                new UniversalNodeConfig(
                        "Node-1",
                        UUID.randomUUID(),
                        new ProtocolAddress("127.0.0.1", 8876),
                        new RandomString(10).nextString(),
                        false,
                        2,
                        1000000L,
                        new ProtocolAddress[0]
                ), new UniversalCloudMessages());
    }

}
