package cloud.hytora.remote.impl;

import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.packets.player.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerLoginPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemotePlayerManager extends DefaultPlayerManager {


    public RemotePlayerManager(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueId, @NotNull String username) {
        CloudPlayer cloudPlayer = new DefaultCloudPlayer(uniqueId, username);

        this.cachedCloudPlayers.put(uniqueId, cloudPlayer);
        Remote.getInstance().getEventManager().callEvent(new CloudPlayerLoginEvent(cloudPlayer));
        Remote.getInstance().getClient().sendPacket(new CloudPlayerLoginPacket(username, uniqueId));
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        Remote.getInstance().getEventManager().callEvent(new CloudPlayerDisconnectEvent(this.cachedCloudPlayers.remove(uuid)));
        Remote.getInstance().getClient().sendPacket(new CloudPlayerDisconnectPacket(uuid, username));
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer) {
        Remote.getInstance().getClient().sendPacket(new CloudPlayerUpdatePacket(cloudPlayer));
    }

}
