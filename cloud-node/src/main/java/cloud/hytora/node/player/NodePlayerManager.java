package cloud.hytora.node.player;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverUpdatePacket;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.player.packet.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.player.packet.CloudPlayerLoginPacket;
import cloud.hytora.driver.player.packet.CloudPlayerUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.player.impl.DefaultTemporaryProperties;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.uuid.IdentificationCache;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.database.DatabaseSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NodePlayerManager extends DefaultPlayerManager {

    public NodePlayerManager() {

        IHandlerNetworkExecutor executor = CloudDriver.getInstance().getNetworkExecutor();
        IEventManager eventManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class);

        executor.registerPacketHandler((PacketHandler<CloudPlayerLoginPacket>) (wrapper, packet) -> {
            CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] logged in on {}!", packet.getUsername(), packet.getUuid(), packet.getProxy());
            ICloudPlayer cloudPlayer = constructPlayer(packet.getUuid(), packet.getUsername());
            cloudPlayer.setProxyServer(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getService(packet.getProxy()));


            IdentificationCache cache = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IdentificationCache.class);
            if (cache.getUUID(cloudPlayer.getName()) == null) {
                cache.setUUID(cloudPlayer.getName(), cloudPlayer.getUniqueId());
                cache.update();
            }

            CloudDriver.getInstance().getProviderRegistry().get(PermissionManager.class).ifPresent(permissionManager -> {
                permissionManager.getPlayerAsyncByUniqueId(cloudPlayer.getUniqueId()).onTaskSucess(player -> {
                    if (player == null) {
                        player = permissionManager.createPlayer(packet.getUsername(), packet.getUuid());
                        permissionManager.updatePermissionPlayer(player);
                    }
                    CloudDriver.getInstance().getLogger().debug("Loaded PermissionPlayer[name={}, uuid={}] into cache!", player.getName(), player.getUniqueId());
                });
            });

            getOfflinePlayerByUniqueIdAsync(cloudPlayer.getUniqueId())
                    .onTaskSucess(cloudOfflinePlayer -> {
                        if (cloudOfflinePlayer == null) {
                            //logged in for the first time probably

                            cloudPlayer.setFirstLogin(System.currentTimeMillis());
                            cloudPlayer.setProperties(DocumentFactory.newJsonDocument());
                            cloudPlayer.saveOfflinePlayer();
                            CloudDriver.getInstance().getLogger().debug("Created DatabaseEntry for Player[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());
                        } else {
                            cloudPlayer.setProperties(cloudOfflinePlayer.getProperties());
                            cloudPlayer.setFirstLogin(cloudOfflinePlayer.getFirstLogin());
                            ((UniversalCloudPlayer)cloudPlayer).setTemporaryProperties((DefaultTemporaryProperties) cloudOfflinePlayer.getTemporaryProperties());

                            cloudPlayer.setLastLogin(System.currentTimeMillis());
                            cloudPlayer.saveOfflinePlayer();
                            cloudPlayer.update();
                        }
                    });

            this.cachedCloudPlayers.put(packet.getUuid(), cloudPlayer);
            eventManager.callEventGlobally(new CloudPlayerLoginEvent(cloudPlayer));

            DriverUpdatePacket.publishUpdate(NodeDriver.getInstance().getNetworkExecutor());
        });

        executor.registerPacketHandler((PacketHandler<CloudPlayerDisconnectPacket>) (wrapper, packet) -> {
            this.getCloudPlayer(packet.getUuid()).ifPresent(cloudPlayer -> {
                if (cloudPlayer == null) {
                    return;
                }
                CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] dissconnected from [proxy={}, server={}]!", cloudPlayer.getName(), cloudPlayer.getUniqueId(), cloudPlayer.getProxyServer() == null ? "No Proxy" : cloudPlayer.getProxyServer().getName(), (cloudPlayer.getServer() == null ? "none" : cloudPlayer.getServer().getName()));
                this.cachedCloudPlayers.remove(cloudPlayer.getUniqueId());
                eventManager.callEventGlobally(new CloudPlayerDisconnectEvent(cloudPlayer));
                if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
                    DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
                }

            });
        });
    }

    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync() {
        return Task.callAsync(new Callable<Collection<CloudOfflinePlayer>>() {
            @Override
            public Collection<CloudOfflinePlayer> call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.getAll();
            }
        });
    }

    @Override
    public @Nullable CloudOfflinePlayer getOfflinePlayerByUniqueIdBlockingOrNull(@NotNull UUID uniqueId) {

        SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
        return db.findById(uniqueId.toString());
    }

    @Override
    public @NotNull Collection<CloudOfflinePlayer> getAllOfflinePlayersBlockingOrEmpty() {
        SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
        return db.getAll();
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.findById(uniqueId.toString());
            }
        });
    }



    @Override
    public @Nullable CloudOfflinePlayer getOfflinePlayerByNameBlockingOrNull(@NotNull String name) {
        SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
        return db.findByMatch("name", name);
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name) {
        return Task.callAsync(new Callable<CloudOfflinePlayer>() {
            @Override
            public CloudOfflinePlayer call() throws Exception {
                SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
                DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
                return db.findByMatch("name", name);
            }
        });
    }


    @Override
    public void saveOfflinePlayerAsync(@NotNull CloudOfflinePlayer player) {
        Task.runAsync(() -> {
            SectionedDatabase database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
            DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            db.upsert(player);

            Logger.constantInstance().debug("Saving OfflinePlayer[name={}, uuid={}]", player.getName(), player.getUniqueId());
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username) {
        this.cachedCloudPlayers.put(uniqueID, constructPlayer(uniqueID, username));
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public ICloudPlayer constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        CloudOfflinePlayer offlinePlayer = getOfflinePlayerByUniqueIdBlockingOrNull(uniqueId);
        return offlinePlayer == null ? new UniversalCloudPlayer(uniqueId, name) : new UniversalCloudPlayer(uniqueId, name, offlinePlayer.getFirstLogin(), offlinePlayer.getLastLogin(), null, null, offlinePlayer.getProperties(), offlinePlayer.getTemporaryProperties());
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name) {
        this.cachedCloudPlayers.remove(uuid);
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer) {
        //Update cache of every component
        CloudPlayerUpdatePacket packet = new CloudPlayerUpdatePacket(cloudPlayer);
        NodeDriver.getInstance().getNetworkExecutor().sendPacketToAll(packet);
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }
}
