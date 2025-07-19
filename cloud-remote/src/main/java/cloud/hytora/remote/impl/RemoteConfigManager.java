package cloud.hytora.remote.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.config.ConfigManager;
import cloud.hytora.driver.config.IConfig;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.config.def.*;
import cloud.hytora.driver.networking.packets.other.PacketNetworkConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoteConfigManager implements ConfigManager, PacketHandler<PacketNetworkConfig> {

    private IConfig config;

    public RemoteConfigManager() {
        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
    }

    @Override
    public INetworkConfig getConfig() {
        return (INetworkConfig) config;
    }

    @Override
    public boolean isRemote() {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    @Override
    public IConfig readBaseConfig() {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    @Override
    public IConfig getBaseConfig() {
        if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.NODE) {
            return config;

        }
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    @Override
    public INetworkConfig readConfig() {
        return (INetworkConfig) (config = PacketNetworkConfig.forGetConfig()
                .sendQuery()
                .execute()
                .syncUninterruptedly()
                .get()
                .buffer()
                .readObject(UniversalNetworkConfig.class));
    }

    @Override
    public void save() {
        config.update();
    }

    @Override
    public void save(IConfig config) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    @Override
    public void handle(PacketChannel channel, PacketNetworkConfig packet) {

        PacketBuffer buffer = packet.buffer();
        PacketNetworkConfig.PayLoad payLoad = buffer.readEnum(PacketNetworkConfig.PayLoad.class);

        switch (payLoad) {
            case UPDATE:
                config = buffer.readObject(UniversalNetworkConfig.class);
                boolean sendBack = buffer.readBoolean();
                if (sendBack) {
                    CloudDriver.getInstance().getExecutor().sendPacket(PacketNetworkConfig.forUpdateConfig((INetworkConfig) config, false));
                }
                break;

            case GET:
                //not used on remote side
                break;
        }
    }
}
