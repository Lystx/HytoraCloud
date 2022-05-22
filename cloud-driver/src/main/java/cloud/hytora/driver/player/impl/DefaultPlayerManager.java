package cloud.hytora.driver.player.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.CloudEventHandler;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerUpdateEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.networking.packets.player.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerLoginPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DefaultPlayerManager implements PlayerManager {

    protected Map<UUID, CloudPlayer> cachedCloudPlayers = new ConcurrentHashMap<>();


    @Override
    public void setAllCachedCloudPlayers(List<CloudPlayer> allCachedCloudPlayers) {
        Map<UUID, CloudPlayer> cloudPlayerMap = new ConcurrentHashMap<>();
        for (CloudPlayer cloudPlayer : allCachedCloudPlayers) {
            cloudPlayerMap.put(cloudPlayer.getUniqueId(), cloudPlayer);
        }
        this.setCachedCloudPlayers(cloudPlayerMap);

    }


    public DefaultPlayerManager(EventManager eventManager) {

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();

        executor.registerPacketHandler((PacketHandler<CloudPlayerUpdatePacket>) (wrapper, packet) -> {

            CloudPlayer player = packet.getPlayer();

            this.getCloudPlayer(player.getUniqueId()).ifPresent(cp -> {
                cp.cloneInternally(player, cp);
                if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                    cp.update();
                }
                eventManager.callEvent(new CloudPlayerUpdateEvent(cp));
            });
        });

        executor.registerPacketHandler((PacketHandler<CloudPlayerLoginPacket>) (wrapper, packet) -> {
            CloudPlayer cloudPlayer = new DefaultCloudPlayer(packet.getUuid(), packet.getUsername());
            this.cachedCloudPlayers.put(packet.getUuid(), cloudPlayer);
            eventManager.callEvent(new CloudPlayerLoginEvent(cloudPlayer));
        });

        executor.registerPacketHandler((PacketHandler<CloudPlayerDisconnectPacket>) (wrapper, packet) -> {
            this.getCloudPlayer(packet.getUuid()).ifPresent(cloudPlayer -> {
                this.cachedCloudPlayers.remove(cloudPlayer.getUniqueId());
                eventManager.callEvent(new CloudPlayerDisconnectEvent(cloudPlayer));
            });
        });

        eventManager.registerListener(this);

    }

    @CloudEventHandler
    public void handle(CloudServerCacheUnregisterEvent event) {

        this.cachedCloudPlayers.values().forEach(player -> {
            if (player.getProxyServer() == null || player.getProxyServer().getName().equals(event.getService())) {
                this.cachedCloudPlayers.remove(player.getUniqueId());
            }
        });
    }


    public void setCachedCloudPlayers(final Map<UUID, CloudPlayer> cachedCloudPlayers) {
        this.cachedCloudPlayers = cachedCloudPlayers;
    }

    public abstract void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username);

    public abstract void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name);

    public abstract void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer);

    @Override
    public @NotNull List<CloudPlayer> getAllCachedCloudPlayers() {
        return Arrays.asList(this.cachedCloudPlayers.values().toArray(new CloudPlayer[0]));
    }

    @Override
    public @NotNull Optional<CloudPlayer> getCloudPlayer(final @NotNull UUID uniqueId) {
        return Optional.ofNullable(this.cachedCloudPlayers.get(uniqueId));
    }

    @Override
    public @NotNull Optional<CloudPlayer> getCloudPlayer(final @NotNull String username) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getName().equalsIgnoreCase(username)).findAny();
    }

    @Override
    public CloudPlayer getCloudPlayerByNameOrNull(@NotNull String username) {
        return this.getCloudPlayer(username).orElse(null);
    }

    @Override
    public CloudPlayer getCloudPlayerByUniqueIdOrNull(@NotNull UUID uniqueId) {
        return this.getCloudPlayer(uniqueId).orElse(null);
    }

}
