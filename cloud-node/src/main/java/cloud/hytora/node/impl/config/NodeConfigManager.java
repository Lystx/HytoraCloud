package cloud.hytora.node.impl.config;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.RandomString;
import cloud.hytora.common.misc.Util;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.*;
import cloud.hytora.driver.config.def.*;
import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.networking.packets.other.PacketNetworkConfig;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.remote.impl.RemoteNetworkConfig;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class NodeConfigManager implements ConfigManager {

    private boolean needsSetup;
    private IConfig config, baseConfig;


    public INetworkConfig getConfig() {
        return (INetworkConfig) config;
    }

    @Override
    public boolean isRemote() {

        if (config != null) {
            return config.getNodeConfig().isRemote();
        }
        Document document = Document.json(CloudDriver.Constants.CONFIG_FILE);
        return document.getDocument("nodeConfig").getBoolean("remote");
    }

    public NodeConfigManager() {
        Util.executeIf(() -> {
            CloudDriver.getInstance().getExecutor().registerPacketHandler(new PacketHandler<PacketNetworkConfig>() {
                @Override
                public void handle(PacketChannel channel, PacketNetworkConfig packet) {
                    PacketBuffer buffer = packet.buffer();

                    switch (packet.getPayLoad()) {
                        case UPDATE:
                            config = buffer.readObject(UniversalNetworkConfig.class);
                            boolean sendBack = buffer.readBoolean();
                            if (sendBack) {
                                NodeDriver.getInstance().getExecutor().sendPacketToAll(PacketNetworkConfig.forUpdateConfig((INetworkConfig) config, false));
                            }
                            save();
                            break;

                        case GET:
                            packet.sendResponse()
                                    .setState(NetworkResponseState.OK)
                                    .setBuffer(buf -> buf.writeObject((config == null ? UniversalNetworkConfig.getDefault() : config)))
                                    .execute();
                            break;
                    }

                }
            });
        }, () -> CloudDriver.getInstance().getExecutor() != null);
    }


    @Override
    public IConfig readBaseConfig() {
        try {

            Logger.constantInstance().trace("Reading config.json (NodeConfiguration)...");
            if (CloudDriver.Constants.CONFIG_FILE.exists()) {
                Logger.constantInstance().trace("Config-File does exist ==> Reading existing config...");
                return baseConfig = Document.json(CloudDriver.Constants.CONFIG_FILE).toInstance(UniversalNetworkConfig.class);
            } else {
                Logger.constantInstance().trace("Config-File does not exist ==> Creating and saving default config..");
                return baseConfig = new RemoteNetworkConfig(
                        new UniversalNodeConfig(
                                "Node-1",
                                UUID.randomUUID(),
                                new ProtocolAddress("127.0.0.1", 8876),
                                new RandomString(10).nextString(),
                                false,
                                2,
                                1000000L,
                                new ProtocolAddress[0]
                        ),
                        new UniversalDatabaseConfig(
                                DatabaseType.FILE,
                                "127.0.0.1",
                                3306,
                                "cloud",
                                "",
                                "cloud",
                                "password123"
                        ));
            }
        } catch (Exception e) {
            Logger.constantInstance().error("Could not load config");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IConfig getBaseConfig() {
        return baseConfig;
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    public INetworkConfig readConfig() {
        try {

            Logger.constantInstance().trace("Reading config.json (NodeConfiguration)...");
            if (CloudDriver.Constants.CONFIG_FILE.exists()) {
                Logger.constantInstance().trace("Config-File does exist ==> Reading existing config...");
                this.needsSetup = false;
                this.config = Document.json(CloudDriver.Constants.CONFIG_FILE).toInstance(UniversalNetworkConfig.class);
            } else {
                this.needsSetup = true;
                this.config = UniversalNetworkConfig.getDefault();
                Logger.constantInstance().trace("Config-File does not exist ==> Creating and saving default config..");
            }

            if (this.config.getNodeConfig().getClusterAddresses().length > 0) {
                this.config.getNodeConfig().setRemote();
            }
            Logger.constantInstance().trace("Config loaded successfully!");
            return (INetworkConfig) this.config;
        } catch (Exception e) {
            Logger.constantInstance().error("Could not load config");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(IConfig config) {
        try {
            Logger.constantInstance().trace("Current Configuration was saved in config.json!");
            Document.json(config).saveToFile(CloudDriver.Constants.CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        this.save(this.config);
    }
}
