package cloud.hytora.remote.impl;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.player.packet.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.player.packet.CloudPlayerLoginPacket;
import cloud.hytora.driver.player.packet.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.packet.OfflinePlayerRequestPacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RemotePlayerManager extends DefaultPlayerManager {


    @Override
    public ICloudPlayer constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        return new UniversalCloudPlayer(uniqueId, name);
    }

    @Override
    public @NotNull IPromise<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync() {
        return IPromise.callAsync(new Callable<Collection<CloudOfflinePlayer>>() {
            @Override
            public Collection<CloudOfflinePlayer> call() throws Exception {
                return CloudDriver.getInstance()
                        .getNetworkExecutor()
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
    public @NotNull IPromise<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId) {
        return IPromise.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                return Remote.getInstance()
                        .getNetworkExecutor()
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
        IPromise.runAsync(() -> Remote.getInstance().getNetworkExecutor().sendPacket(new OfflinePlayerRequestPacket(player)));
    }

    @Override
    public @NotNull IPromise<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name) {
        return IPromise.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                return Remote.getInstance()
                        .getNetworkExecutor()
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
        ICloudPlayer cloudPlayer = constructPlayer(uniqueId, username);
        cloudPlayer.setProxyServer(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).thisServiceOrNull());

        this.cachedCloudPlayers.put(uniqueId, cloudPlayer);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new CloudPlayerLoginEvent(cloudPlayer));
        Remote.getInstance().getNetworkExecutor().sendPacket(new CloudPlayerLoginPacket(username, uniqueId, cloudPlayer.getProxyServer().getName()));
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        if (this.getCloudPlayerByUniqueIdOrNull(uuid) == null) {
            return;
        }
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new CloudPlayerDisconnectEvent(this.cachedCloudPlayers.remove(uuid)));
        Remote.getInstance().getNetworkExecutor().sendPacket(new CloudPlayerDisconnectPacket(uuid, username));
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer) {
        Remote.getInstance().getNetworkExecutor().sendPacket(new CloudPlayerUpdatePacket(cloudPlayer));
    }

}
