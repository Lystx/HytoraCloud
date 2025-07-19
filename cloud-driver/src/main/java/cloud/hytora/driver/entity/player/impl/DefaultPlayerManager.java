package cloud.hytora.driver.entity.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class DefaultPlayerManager implements PlayerManager {

    protected Map<UUID, CloudPlayer> cachedCloudPlayers = new ConcurrentHashMap<>();
    protected Map<UUID, CloudOfflinePlayer> cachedOfflinePlayers = new ConcurrentHashMap<>();

    @Override
    public void setCachedCloudPlayers(Collection<CloudPlayer> allCachedCloudPlayers) {
        Map<UUID, CloudPlayer> cloudPlayerMap = new ConcurrentHashMap<>();
        for (CloudPlayer cloudPlayer : allCachedCloudPlayers) {
            cloudPlayerMap.put(cloudPlayer.getUniqueId(), cloudPlayer);
        }
        this.setCachedCloudPlayers(cloudPlayerMap);

    }


    public DefaultPlayerManager(EventManager eventManager) {

        HandlingNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        if (executor == null) {
            return;
        }
        eventManager.registerListener(this);

    }


    @EventListener
    public void handle(CloudEventServiceUnregistered event) {

        this.cachedCloudPlayers.values().forEach(player -> {
            if (player.getProxyServer() == null || player.getProxyServer().getName().equals(event.getService())) {
                this.cachedCloudPlayers.remove(player.getUniqueId());
            }
        });
    }


    @NotNull
    @Override
    public Collection<CloudOfflinePlayer> getCachedOfflinePlayers() {
        return cachedOfflinePlayers.values();
    }

    @Override
    public CloudOfflinePlayer getCachedOfflinePlayerOrRefresh(@NotNull String name) {
        if (getCachedCloudPlayer(name) != null) {
            return getCachedCloudPlayer(name);
        }
        if (getCachedOfflinePlayers().stream().anyMatch(op -> op.getName().equalsIgnoreCase(name))) {
            return getCachedOfflinePlayers().stream().filter(op -> op.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
        return getOfflinePlayer(name).syncUninterruptedly().get();
    }


    @Override
    public CloudOfflinePlayer getCachedOfflinePlayerOrRefresh(@NotNull UUID uniqueId) {
        if (getCachedCloudPlayer(uniqueId) != null) {
            return getCachedCloudPlayer(uniqueId);
        }
        if (getCachedOfflinePlayers().stream().anyMatch(op -> op.getUniqueId().equals(uniqueId))) {
            return getCachedOfflinePlayers().stream().filter(op -> op.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
        }
        return getOfflinePlayer(uniqueId).syncUninterruptedly().get();
    }

    public void setCachedCloudPlayers(Map<UUID, CloudPlayer> cachedCloudPlayers) {
        this.cachedCloudPlayers = cachedCloudPlayers;
    }

    public abstract void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name);


    public void registerPlayer(CloudPlayer player) {
        this.cachedCloudPlayers.put(player.getUniqueId(), player);
    }

    @Override
    public abstract void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer, PublishingType... type);

    public abstract Task<CloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name);

    @Override
    public @NotNull Collection<CloudPlayer> getAllCachedCloudPlayers() {
        return this.cachedCloudPlayers.values();
    }

    @Override
    public CloudPlayer getCachedCloudPlayer(@NotNull String username) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getName().equalsIgnoreCase(username)).findAny().orElse(null);
    }

    @Override
    public CloudPlayer getCachedCloudPlayer(@NotNull UUID uniqueId) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getUniqueId().equals(uniqueId)).findAny().orElse(null);
    }

}
