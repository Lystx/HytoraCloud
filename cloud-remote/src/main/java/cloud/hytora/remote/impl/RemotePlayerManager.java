package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.player.packet.PacketOfflinePlayer;
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
import java.util.function.Consumer;
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
                        .execute(new PacketOfflinePlayer())
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
        Task<CloudOfflinePlayer> task = Task.empty();

        ICloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(uniqueId);
        if (cachedCloudPlayer != null) {
            task.setResult(cachedCloudPlayer);
        } else {

            Remote.getInstance()
                    .getClient()
                    .getPacketChannel()
                    .prepareSingleQuery()
                    .execute(new PacketOfflinePlayer(uniqueId))
                    .onTaskSucess(e -> {
                        PacketBuffer buffer = e.buffer();
                        DefaultCloudOfflinePlayer player = buffer.readOptionalObject(DefaultCloudOfflinePlayer.class);
                        task.setResult(player);
                    }).onTaskFailed(task::setFailure);
        }
        return task;
        /*return Task.callAsync(() -> Remote.getInstance()
                .getClient()
                .getPacketChannel()
                .prepareSingleQuery()
                .execute(new OfflinePlayerRequestPacket(uniqueId))
                .allowNull()
                .syncUninterruptedly()
                .get()
                .buffer()
                .readOptionalObject(DefaultCloudOfflinePlayer.class));*/
    }

    @Override
    public Task<Void> saveOfflinePlayer(@NotNull CloudOfflinePlayer player) {
        return Task.runAsync(() -> Remote.getInstance().getClient().sendPacket(new PacketOfflinePlayer(player)));
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull String name) {
        Task<CloudOfflinePlayer> task = Task.empty();

        ICloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(name);
        if (cachedCloudPlayer != null) {
            task.setResult(cachedCloudPlayer);
        } else {

            Remote.getInstance()
                    .getClient()
                    .getPacketChannel()
                    .prepareSingleQuery()
                    .execute(new PacketOfflinePlayer(name))
                    .onTaskSucess(e -> {
                        PacketBuffer buffer = e.buffer();
                        DefaultCloudOfflinePlayer player = buffer.readOptionalObject(DefaultCloudOfflinePlayer.class);
                        task.setResult(player);
                    }).onTaskFailed(task::setFailure);
        }
        return task;
    }

    @Override
    public boolean hasJoinedTheNetworkBefore(UUID uniqueId, Consumer<CloudOfflinePlayer> handler) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE); // TODO: 29.04.2025 implement
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    public void unregister(UUID id) {
        this.cachedCloudPlayers.remove(id);
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer, PublishingType... type) {
        PublishingType publishingType = PublishingType.get(type);
        switch (publishingType) {
            case INTERNAL:
                cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
                break;
            case PROTOCOL:
                PacketCloudPlayer.forPlayerUpdate(cloudPlayer).publish();
                break;
            case GLOBAL:
                updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
                updateCloudPlayer(cloudPlayer, PublishingType.PROTOCOL);
                break;
        }
    }

}
