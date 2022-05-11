package cloud.hytora.remote.impl;


import cloud.hytora.driver.services.configuration.DefaultConfigurationManager;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteConfigurationManager extends DefaultConfigurationManager {

    @Override
    public void update(@NotNull ServerConfiguration serviceGroup) {
        Remote.getInstance().getClient().sendPacket(new ServerConfigurationCacheUpdatePacket(serviceGroup));
    }

}
