package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.player.packet.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.player.packet.CloudPlayerLoginPacket;
import cloud.hytora.driver.player.packet.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.packet.OfflinePlayerRequestPacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
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
    public Task<ICloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        Task<ICloudPlayer> task = Task.empty();
        task.setResult(new UniversalCloudPlayer(uniqueId, name));
        return task;
    }

    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getOfflinePlayers() {
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
    // TODO: 15.04.2025 offline player cache 
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull UUID uniqueId) {
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
    public Task<Void> saveOfflinePlayer(@NotNull CloudOfflinePlayer player) {
        return Task.runAsync(() -> Remote.getInstance().getClient().sendPacket(new OfflinePlayerRequestPacket(player)));
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull String name) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                return Remote.getInstance()
                        .getClient()
                        .getPacketChannel()
                        .prepareSingleQuery()
                        .execute(new OfflinePlayerRequestPacket(name))
                        .syncUninterruptedly()
                        .allowNull()
                        .get()
                        .buffer()
                        .readOptionalObject(DefaultCloudOfflinePlayer.class);
            }
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueId, @NotNull String username) {
        if (getCachedCloudPlayer(username) != null || getCachedCloudPlayer(uniqueId) != null) {
            return;
        }
        constructPlayer(uniqueId, username).onTaskSucess(cloudPlayer -> {

            cloudPlayer.setProxyServer(CloudDriver.getInstance().getServiceManager().thisServiceOrNull());

            this.cachedCloudPlayers.put(uniqueId, cloudPlayer);
            Remote.getInstance().getClient().sendPacket(new CloudPlayerLoginPacket(username, uniqueId, cloudPlayer.getProxyServer().getName()));
        }).onTaskFailed(e -> {
            System.out.println("REMOTEPLAYERMANAGER -> " + e);
        });
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        if (this.getCachedCloudPlayer(uuid) == null) {
            return;
        }
        Remote.getInstance().getEventManager().callEventGlobally(new CloudPlayerDisconnectEvent(this.cachedCloudPlayers.remove(uuid)));
        Remote.getInstance().getClient().sendPacket(new CloudPlayerDisconnectPacket(uuid, username));
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer) {
        Remote.getInstance().getClient().sendPacket(new CloudPlayerUpdatePacket(cloudPlayer));
    }

}
