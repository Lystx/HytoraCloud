package cloud.hytora.driver.player.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class DefaultPlayerManager implements PlayerManager {

    protected Map<UUID, ICloudPlayer> cachedCloudPlayers = new ConcurrentHashMap<>();


    @Override
    public void setAllCachedCloudPlayers(List<ICloudPlayer> allCachedCloudPlayers) {
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

            this.getCloudPlayer(player.getUniqueId()).ifPresent(cp -> {
                cp.clone(player);
                if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                    cp.update();
                }
                eventManager.callEventGlobally(new CloudPlayerUpdateEvent(cp));
            });
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

    @Override
    public @Nullable CloudOfflinePlayer getOfflinePlayerByUniqueIdBlockingOrNull(@NotNull UUID uniqueId) {
        return getOfflinePlayerByUniqueIdAsync(uniqueId).timeOut(TimeUnit.SECONDS, 12).syncUninterruptedly().orElse(null);
    }

    @Override
    public @Nullable CloudOfflinePlayer getOfflinePlayerByNameBlockingOrNull(@NotNull String name) {
        return getOfflinePlayerByNameAsync(name).syncUninterruptedly().orElse(null);
    }

    @Override
    public @NotNull Collection<CloudOfflinePlayer> getAllOfflinePlayersBlockingOrEmpty() {
        return getAllOfflinePlayersAsync().syncUninterruptedly().orElse(new ArrayList<>());
    }

    public abstract void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username);

    public abstract void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name);

    public abstract void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer);

    public abstract ICloudPlayer constructPlayer(@NotNull UUID uniqueId, @NotNull String name);

    @Override
    public @NotNull List<ICloudPlayer> getAllCachedCloudPlayers() {
        return Arrays.asList(this.cachedCloudPlayers.values().toArray(new ICloudPlayer[0]));
    }

    @Override
    public @NotNull Optional<ICloudPlayer> getCloudPlayer(final @NotNull UUID uniqueId) {
        return Optional.ofNullable(this.cachedCloudPlayers.get(uniqueId));
    }

    @Override
    public @NotNull Optional<ICloudPlayer> getCloudPlayer(final @NotNull String username) {
        return this.cachedCloudPlayers.values().stream().filter(it -> it.getName().equalsIgnoreCase(username)).findAny();
    }

    @Override
    public ICloudPlayer getCloudPlayerByNameOrNull(@NotNull String username) {
        return this.getCloudPlayer(username).orElse(null);
    }

    @Override
    public ICloudPlayer getCloudPlayerByUniqueIdOrNull(@NotNull UUID uniqueId) {
        return this.getCloudPlayer(uniqueId).orElse(null);
    }

}
