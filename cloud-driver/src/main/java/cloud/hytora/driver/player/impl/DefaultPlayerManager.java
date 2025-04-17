package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.player.packet.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class DefaultPlayerManager implements PlayerManager {

    @Setter
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
        executor.registerPacketHandler((PacketHandler<CloudPlayerUpdatePacket>) (wrapper, packet) -> {

            ICloudPlayer player = packet.getPlayer();
            ICloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(player.getUniqueId());
            if (cachedCloudPlayer != null) {
                cachedCloudPlayer.clone(player);
                if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                    cachedCloudPlayer.update();
                }
                cachedCloudPlayers.put(player.getUniqueId(), cachedCloudPlayer);
                eventManager.callEventGlobally(new CloudPlayerUpdateEvent(cachedCloudPlayer));
            }
        });


        eventManager.registerListener(this);

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

    public abstract void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username);

    public abstract void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name);

    public abstract void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer);

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
