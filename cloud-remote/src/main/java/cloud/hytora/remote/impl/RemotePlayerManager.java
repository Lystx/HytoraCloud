package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.packets.player.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerLoginPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.networking.packets.player.OfflinePlayerRequestPacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RemotePlayerManager extends DefaultPlayerManager {

    public RemotePlayerManager(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    public CloudPlayer constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        return new DefaultCloudPlayer(uniqueId, name);
    }

    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync() {
        return Task.callAsync(new Callable<Collection<CloudOfflinePlayer>>() {
            @Override
            public Collection<CloudOfflinePlayer> call() throws Exception {
                return CloudDriver.getInstance()
                        .getExecutor()
                        .getPacketChannel()
                        .prepareSingleQuery()
                        .execute(new OfflinePlayerRequestPacket())
                        .syncUninterruptedly()
                        .get()
                        .buffer()
                        .readObjectCollection(DefaultCloudOfflinePlayer.class)
                        .stream()
                        .map(c -> ((CloudOfflinePlayer) c))
                        .collect(Collectors.toList());
            }
        });
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                return Remote.getInstance()
                        .getClient()
                        .getPacketChannel()
                        .prepareSingleQuery()
                        .execute(new OfflinePlayerRequestPacket(uniqueId))
                        .syncUninterruptedly()
                        .get()
                        .buffer()
                        .readOptionalObject(DefaultCloudOfflinePlayer.class);
            }
        });
    }

    @Override
    public void saveOfflinePlayerAsync(@NotNull CloudOfflinePlayer player) {
        Task.runAsync(() -> Remote.getInstance().getClient().sendPacket(new OfflinePlayerRequestPacket(player)));
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                return Remote.getInstance()
                        .getClient()
                        .getPacketChannel()
                        .prepareSingleQuery()
                        .execute(new OfflinePlayerRequestPacket(name))
                        .syncUninterruptedly()
                        .get()
                        .buffer()
                        .readOptionalObject(DefaultCloudOfflinePlayer.class);
            }
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueId, @NotNull String username) {
        CloudPlayer cloudPlayer = constructPlayer(uniqueId, username);
        cloudPlayer.setProxyServer(CloudDriver.getInstance().getServiceManager().thisServiceOrNull());

        this.cachedCloudPlayers.put(uniqueId, cloudPlayer);
        Remote.getInstance().getEventManager().callEventGlobally(new CloudPlayerLoginEvent(cloudPlayer));
        Remote.getInstance().getClient().sendPacket(new CloudPlayerLoginPacket(username, uniqueId, cloudPlayer.getProxyServer().getName()));
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        if (this.getCloudPlayerByUniqueIdOrNull(uuid) == null) {
            return;
        }
        Remote.getInstance().getEventManager().callEventGlobally(new CloudPlayerDisconnectEvent(this.cachedCloudPlayers.remove(uuid)));
        Remote.getInstance().getClient().sendPacket(new CloudPlayerDisconnectPacket(uuid, username));
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer) {
        Remote.getInstance().getClient().sendPacket(new CloudPlayerUpdatePacket(cloudPlayer));
    }

}
