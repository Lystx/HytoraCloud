package cloud.hytora.node.impl.player;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.packet.CloudPlayerDisconnectPacket;
import cloud.hytora.driver.player.packet.CloudPlayerLoginPacket;
import cloud.hytora.driver.player.packet.CloudPlayerUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.database.LocalStorage;
import cloud.hytora.driver.database.LocalStorageSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class NodePlayerManager extends DefaultPlayerManager {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();

        executor.registerPacketHandler((PacketHandler<CloudPlayerLoginPacket>) (wrapper, packet) -> {
            CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] logged in on {}!", packet.getUsername(), packet.getUuid(), packet.getProxy());

            constructPlayer(packet.getUuid(), packet.getUsername())
                    .onTaskSucess(cloudPlayer -> {
                        cloudPlayer.setProxyServer(CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(packet.getProxy()));


                        if (cloudPlayer.getLastLogin() == -1L) { //logged in for first time

                            //logged in for the first time probably

                            cloudPlayer.setFirstLogin(System.currentTimeMillis());
                            cloudPlayer.setLastLogin(System.currentTimeMillis());
                            cloudPlayer.setProperties(DocumentFactory.newJsonDocument());
                            cloudPlayer.saveOfflinePlayer();
                            CloudDriver.getInstance().getLogger().debug("Created DatabaseEntry for Player[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());
                        } else { //has logged in before
                            cloudPlayer.setLastLogin(System.currentTimeMillis());
                            cloudPlayer.saveOfflinePlayer();
                            cloudPlayer.update();
                        }


                        /*CloudDriver.getInstance().getProviderRegistry().get(PermissionManager.class).ifPresent(permissionManager -> {
                            permissionManager.getPlayerAsyncByUniqueId(cloudPlayer.getUniqueId()).onTaskSucess(player -> {
                                if (player == null) {
                                    player = permissionManager.createPlayer(packet.getUsername(), packet.getUuid());
                                    permissionManager.updatePermissionPlayer(player);
                                }
                                CloudDriver.getInstance().getLogger().debug("Loaded PermissionPlayer[name={}, uuid={}] into cache!", player.getName(), player.getUniqueId());
                            });
                        });*/


                        this.cachedCloudPlayers.put(packet.getUuid(), cloudPlayer);
                        eventManager.callEventGlobally(new CloudPlayerLoginEvent(cloudPlayer));

                        DriverUpdatePacket.publishUpdate(NodeDriver.getInstance().getExecutor());
                    }).onTaskFailed(e -> {
                        try {
                            throw e;
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    });

        });

        executor.registerPacketHandler((PacketHandler<CloudPlayerDisconnectPacket>) (wrapper, packet) -> {
            ICloudPlayer cloudPlayer = this.getCachedCloudPlayer(packet.getUuid());
            if (cloudPlayer != null) {

                CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] dissconnected from [proxy={}, server={}]!", cloudPlayer.getName(), cloudPlayer.getUniqueId(), cloudPlayer.getProxyServer() == null ? "No Proxy" : cloudPlayer.getProxyServer().getName(), (cloudPlayer.getServer() == null ? "none" : cloudPlayer.getServer().getName()));
                this.cachedCloudPlayers.remove(cloudPlayer.getUniqueId());
                eventManager.callEventGlobally(new CloudPlayerDisconnectEvent(cloudPlayer));
                if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
                    DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor());
                }
            }
        });
    }

    @Override
    public @NotNull Task<Collection<CloudOfflinePlayer>> getOfflinePlayers() {
        Task<Collection<CloudOfflinePlayer>> task = Task.empty();

        Database db = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        db.query("player_data")
                .executeAsync()
                .onTaskSucess(query -> {

                    Collection<CloudOfflinePlayer> players = new ArrayList<>();
                    query.all().forEach(doc -> {
                        CloudOfflinePlayer player = new DefaultCloudOfflinePlayer();
                        player.applyDocument(doc);
                        players.add(player);
                    });
                    task.setResult(players);
                }).onTaskFailed(task::setFailure);


        return task;

       /* return Task.callAsync(() -> {

            LocalStorage database = NodeDriver.getInstance().getDatabaseManager().getLocalStorage();
            LocalStorageSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            return db.getAll();
        });*/
    }

    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull UUID uniqueId) {

        Task<CloudOfflinePlayer> task = Task.empty();

        Database db = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        db.query("player_data")
                .where("uniqueId", uniqueId)
                .executeAsync()
                .onTaskSucess(query -> {
                    if (query.isEmpty()) {
                        task.setResult(null);
                        return;
                    }
                    query.first().ifPresent(doc -> {
                        CloudOfflinePlayer player = new DefaultCloudOfflinePlayer(doc);
                        task.setResult(player);
                    });
                }).onTaskFailed(task::setFailure);


        return task;
        /*return Task.callAsync(() -> {
            LocalStorage database = NodeDriver.getInstance().getDatabaseManager().getLocalStorage();
            LocalStorageSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            return db.findById(uniqueId.toString());
        }).allowNull();*/
    }


    @Override
    public @NotNull Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull String name) {

        Task<CloudOfflinePlayer> task = Task.empty();

        Database db = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        db.query("player_data")
                .where("name", name, true)
                .executeAsync()
                .onTaskSucess(query -> {
                    if (query.isEmpty()) {
                        task.setResult(null);
                        return;
                    }
                    query.first().ifPresent(doc -> {
                        CloudOfflinePlayer player = new DefaultCloudOfflinePlayer(doc);
                        task.setResult(player);
                    });
                }).onTaskFailed(task::setFailure);


        return task;
        /*return Task.callAsync(() -> {
            LocalStorage database = NodeDriver.getInstance().getDatabaseManager().getLocalStorage();
            LocalStorageSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            return db.findByMatch("name", name);
        }).allowNull();*/
    }


    @Override
    public Task<Void> saveOfflinePlayer(@NotNull CloudOfflinePlayer player) {
        return Task.runAsync(() -> {
            if (player.getProperties() == null) {
                return;
            }
            if (player.isOnline()) {
                this.updateCloudPlayer(player.asOnlinePlayer());
            }

            Database db = NodeDriver.getInstance().getDatabaseManager().getDatabase();

            db.insertOrUpdate("player_data")
                    .where("uniqueId", player.getUniqueId())
                    .set("name", player.getName())
                    .set("uniqueId", player.getUniqueId())
                    .set("firstLogin", player.getFirstLogin())
                    .set("lastLogin", player.getLastLogin())
                    .set("properties", player.getProperties().asRawJsonString())
                                    .executeAsync();

            /*
            LocalStorage database = NodeDriver.getInstance().getDatabaseManager().getLocalStorage();
            LocalStorageSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);
            db.upsert(player);*/

            Logger.constantInstance().debug("Saving OfflinePlayer[name={}, uuid={}]", player.getName(), player.getUniqueId());
        });
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username) {
        constructPlayer(uniqueID, username).onTaskSucess(p -> {
            this.updateCloudPlayer(p);
            //this.cachedCloudPlayers.put(uniqueID, p);
            if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
                // DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor()); //not necessary to update whole driver
            }
        }).onTaskFailed(e -> {
            System.out.println("NODEPLAYERMANAGER -> " + e);
        });
    }

    @Override
    public Task<ICloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        Task<ICloudPlayer> task = Task.empty();
        task.allowNull();
        getOfflinePlayer(uniqueId)
                .onTaskSucess(cop -> {
                    if (cop == null) {
                        task.setResult(new UniversalCloudPlayer(uniqueId, name, System.currentTimeMillis(), -1L, null, null, null));
                    } else {
                        task.setResult(
                                new UniversalCloudPlayer(
                                        uniqueId,
                                        name,
                                        cop.getFirstLogin(),
                                        cop.getLastLogin(),
                                        null,
                                        null,
                                        cop.getProperties()
                                )
                        );
                    }
                })
                .onTaskFailed(task::setFailure)
        ;
        return task;
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name) {
        this.cachedCloudPlayers.remove(uuid);
        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer) {
        //Update cache of every component
        cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
        CloudPlayerUpdatePacket packet = new CloudPlayerUpdatePacket(cloudPlayer);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(packet);
        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            //DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getExecutor()); todo check if needed
        }
    }
}
