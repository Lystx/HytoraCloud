package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class DefaultPlayerManager implements PlayerManager {

    protected Map<UUID, ICloudPlayer> cachedCloudPlayers = new ConcurrentHashMap<>();


    @Override
    public void setCachedCloudPlayers(Collection<ICloudPlayer> allCachedCloudPlayers) {
        Map<UUID, ICloudPlayer> cloudPlayerMap = new ConcurrentHashMap<>();
        for (ICloudPlayer cloudPlayer : allCachedCloudPlayers) {
            cloudPlayerMap.put(cloudPlayer.getUniqueId(), cloudPlayer);
        }
        this.setCachedCloudPlayers(cloudPlayerMap);

    }


    public DefaultPlayerManager(EventManager eventManager) {

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        if (executor == null) {
            return;
        }
        eventManager.registerListener(this);

    }


    protected void updateInternal(@Nullable ICloudPlayer player) {
        if (player == null) {
            return;
        }
        ICloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(player.getUniqueId());
        if (cachedCloudPlayer != null) {
            cachedCloudPlayer.clone(player);
            cachedCloudPlayers.put(player.getUniqueId(), cachedCloudPlayer);
        } else {
            cachedCloudPlayers.put(player.getUniqueId(), player);
        }
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {

        this.cachedCloudPlayers.values().forEach(player -> {
            if (player.getProxyServer() == null || player.getProxyServer().getName().equals(event.getService())) {
                this.cachedCloudPlayers.remove(player.getUniqueId());
            }
        });
    }


    public void setCachedCloudPlayers(Map<UUID, ICloudPlayer> cachedCloudPlayers) {
        this.cachedCloudPlayers = cachedCloudPlayers;
    }

    public abstract boolean hasJoinedTheNetworkBefore(UUID uniqueId, Consumer<CloudOfflinePlayer> handler);

    public abstract void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name);


    public void registerPlayer(ICloudPlayer player) {
        this.cachedCloudPlayers.put(player.getUniqueId(), player);
    }

    @Override
    public abstract void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer, PublishingType... type);

    public abstract Task<ICloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name);

    @Override
    public @NotNull Collection<ICloudPlayer> getAllCachedCloudPlayers() {
        return this.cachedCloudPlayers.values();
    }

    @Override
    public ICloudPlayer getCachedCloudPlayer(@NotNull String username) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getName().equalsIgnoreCase(username)).findAny().orElse(null);
    }

    @Override
    public ICloudPlayer getCachedCloudPlayer(@NotNull UUID uniqueId) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getUniqueId().equals(uniqueId)).findAny().orElse(null);
    }

}
