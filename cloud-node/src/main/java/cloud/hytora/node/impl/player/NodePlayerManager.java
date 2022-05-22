package cloud.hytora.node.impl.player;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.QueryState;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.database.impl.section.DatabaseSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NodePlayerManager extends DefaultPlayerManager {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    public @NotNull Wrapper<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync() {
        return Wrapper.callAsync(new Callable<Collection<CloudOfflinePlayer>>() {
            @Override
            public Collection<CloudOfflinePlayer> call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.getAll();
            }
        });
    }


    @Override
    public @NotNull Wrapper<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId) {
        return Wrapper.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.findById(uniqueId.toString());
            }
        });
    }

    @Override
    public @NotNull Wrapper<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name) {
        return Wrapper.callAsync(new Callable<CloudOfflinePlayer>() {
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
        Wrapper.runAsync(() -> {
            SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
            DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            db.upsert(player);
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username) {
        this.cachedCloudPlayers.put(uniqueID, new DefaultCloudPlayer(uniqueID, username));
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
