package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.QueryResponse;
import cloud.hytora.driver.networking.query.QueryState;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.entity.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.impl.extension.RemoteBukkitPlayer;
import cloud.hytora.remote.impl.extension.RemoteProxyPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RemotePlayerManager extends DefaultPlayerManager implements PlayerExtension, PacketHandler<PacketCloudEntityOfflinePlayer> {

    public RemotePlayerManager(EventManager eventManager) {
        super(eventManager);


        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
        CloudDriver.getInstance().setProvider(PlayerExtension.class, this);
    }

    @Override
    public Task<CloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        Task<CloudPlayer> task = Task.empty();
        task.setResult(new UniversalCloudPlayer(uniqueId, name));
        return task;
    }


    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getOfflinePlayers() {
        return Task.callAsync(() -> CloudDriver.getInstance()
                .getExecutor()
                .getPacketChannel()
                .sendQuery()
                .execute(new PacketCloudEntityOfflinePlayer())
                .syncUninterruptedly()
                .get()
                .buffer()
                .readObjectCollection(DefaultCloudOfflinePlayer.class)
                .stream()
                .map(c -> ((CloudOfflinePlayer) c))
                .collect(Collectors.toList()));
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull UUID uniqueId) {
        Task<CloudOfflinePlayer> task = Task.empty();

        CloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(uniqueId);
        if (cachedCloudPlayer != null) {
            task.setResult(cachedCloudPlayer);
        } else {

            Remote.getInstance()
                    .getClient()
                    .getPacketChannel()
                    .sendQuery()
                    .execute(new PacketCloudEntityOfflinePlayer(uniqueId))
                    .onTaskSucess(e -> {
                        PacketBuffer buffer = e.buffer();
                        DefaultCloudOfflinePlayer player = buffer.readOptionalObject(DefaultCloudOfflinePlayer.class);
                        if (player != null) {
                            cachedOfflinePlayers.put(player.getUniqueId(), player);
                        }
                        task.setResult(player);
                    }).onTaskFailed(task::setFailure);
        }
        return task;
    }

    @Override
    public Task<Void> saveOfflinePlayer(@NotNull CloudOfflinePlayer player) {
        this.cachedOfflinePlayers.put(player.getUniqueId(), player);
        return Task.runAsync(() -> Remote.getInstance().getClient().sendPacket(new PacketCloudEntityOfflinePlayer(player)));
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull String name) {
        Task<CloudOfflinePlayer> task = Task.empty();

        CloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(name);
        if (cachedCloudPlayer != null) {
            task.setResult(cachedCloudPlayer);
        } else {

            Remote.getInstance()
                    .getClient()
                    .getPacketChannel()
                    .sendQuery()
                    .execute(new PacketCloudEntityOfflinePlayer(name))
                    .onTaskSucess(e -> {
                        PacketBuffer buffer = e.buffer();
                        DefaultCloudOfflinePlayer player = buffer.readOptionalObject(DefaultCloudOfflinePlayer.class);
                        if (player != null) {
                            cachedOfflinePlayers.put(player.getUniqueId(), player);
                        }
                        task.setResult(player);
                    }).onTaskFailed(task::setFailure);
        }
        return task;
    }


    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String username) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE);
    }

    public void unregister(UUID id) {
        this.cachedCloudPlayers.remove(id);
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer, PublishingType... type) {
        PublishingType publishingType = PublishingType.get(type);
        switch (publishingType) {
            case INTERNAL:
                cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
                break;
            case PROTOCOL:
                PacketCloudEntityPlayer.forPlayerUpdate(cloudPlayer).publish();
                break;
            case GLOBAL:
                updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
                updateCloudPlayer(cloudPlayer, PublishingType.PROTOCOL);
                break;
        }
    }

    @Override
    public CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer) {
        return new RemoteProxyPlayer(cloudPlayer);
    }

    @Override
    public CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer) {
        return new RemoteBukkitPlayer(cloudPlayer);
    }

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityOfflinePlayer packet) {
        PacketBuffer buffer = packet.buffer();
        PacketCloudEntityOfflinePlayer.PayLoad payLoad = buffer.readEnum(PacketCloudEntityOfflinePlayer.PayLoad.class);

        if (payLoad == PacketCloudEntityOfflinePlayer.PayLoad.UPDATE_TO_CACHE) {
            //saving player on this node side
            DefaultCloudOfflinePlayer player = buffer.readObject(DefaultCloudOfflinePlayer.class);
            this.cachedOfflinePlayers.put(player.getUniqueId(), player);
        }
    }
}
