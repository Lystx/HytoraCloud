package cloud.hytora.node.impl.player;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerLoginPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.database.impl.section.DatabaseSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NodePlayerManager extends DefaultPlayerManager {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();

        executor.registerPacketHandler((PacketHandler<CloudPlayerLoginPacket>) (wrapper, packet) -> {
            CloudPlayer cloudPlayer = constructPlayer(packet.getUuid(), packet.getUsername());
            this.cachedCloudPlayers.put(packet.getUuid(), cloudPlayer);
            eventManager.callEvent(new CloudPlayerLoginEvent(cloudPlayer));

            DriverUpdatePacket.publishUpdate(NodeDriver.getInstance());
        });

        executor.registerPacketHandler((PacketHandler<CloudPlayerDisconnectPacket>) (wrapper, packet) -> {
            this.getCloudPlayer(packet.getUuid()).ifPresent(cloudPlayer -> {
                this.cachedCloudPlayers.remove(cloudPlayer.getUniqueId());
                eventManager.callEvent(new CloudPlayerDisconnectEvent(cloudPlayer));
                DriverUpdatePacket.publishUpdate(NodeDriver.getInstance());
            });
        });
    }

    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync() {
        return Task.callAsync(new Callable<Collection<CloudOfflinePlayer>>() {
            @Override
            public Collection<CloudOfflinePlayer> call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.getAll();
            }
        });
    }


    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.findById(uniqueId.toString());
            }
        });
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.findByMatch("name", name);
            }
        });
    }

    @Override
    public void saveOfflinePlayerAsync(@NotNull CloudOfflinePlayer player) {
        Task.runAsync(() -> {
            SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
            DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            db.upsert(player);
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username) {
        this.cachedCloudPlayers.put(uniqueID, constructPlayer(uniqueID, username));
    }

    @Override
    public CloudPlayer constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        CloudOfflinePlayer offlinePlayer = getOfflinePlayerByUniqueIdBlockingOrNull(uniqueId);
        return offlinePlayer == null ? new DefaultCloudPlayer(uniqueId, name) : new DefaultCloudPlayer(uniqueId, name, offlinePlayer.getLastConnection(), offlinePlayer.getFirstLogin(), offlinePlayer.getLastLogin(), offlinePlayer.getProperties(), null, null);
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name) {
        this.cachedCloudPlayers.remove(uuid);
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer) {
        //Update cache of every component
        CloudPlayerUpdatePacket packet = new CloudPlayerUpdatePacket(cloudPlayer);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(packet);
    }
}
