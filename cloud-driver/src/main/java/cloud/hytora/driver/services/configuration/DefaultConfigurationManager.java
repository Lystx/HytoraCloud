package cloud.hytora.driver.services.configuration;

import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.CloudDriver;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class DefaultConfigurationManager implements ConfigurationManager {

    protected List<ServerConfiguration> allCachedConfigurations = new ArrayList<>();

    public DefaultConfigurationManager() {
        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServerConfigurationCacheUpdatePacket>) (wrapper, packet) -> {
            ServerConfiguration packetGroup = packet.getConfiguration();
            ServerConfiguration preset = getConfigurationByNameOrNull(packetGroup.getName());

            preset.cloneInternally(packetGroup, preset);
            if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                preset.update();
            }
        });
    }

    @Override
    public void addConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.allCachedConfigurations.add(serviceGroup);
    }

    public void removeConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.allCachedConfigurations.remove(serviceGroup);
    }

}
