package cloud.hytora.node.impl.player;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.*;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class NodePlayerManager extends DefaultPlayerManager implements PacketHandler<PacketCloudPlayer> {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler(this);
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
        ICloudPlayer cp = getCachedCloudPlayer(uuid);
        if (cp == null) {
            return;
        }

        this.cachedCloudPlayers.remove(cp.getUniqueId());
        CloudDriver.getInstance().getEventManager().callEvent(new CloudPlayerDisconnectEvent(cp), PublishingType.GLOBAL);
        CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] dissconnected from [proxy={}, server={}]!", cp.getName(), cp.getUniqueId(), cp.getProxyServer().getName(), (cp.getServer() == null ? "none" : cp.getServer().getName()));
    }

    @Override
    public void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer, PublishingType... type) {
        //Update cache of every component
        PublishingType publishingType = PublishingType.get(type);
        switch (publishingType) {
            case INTERNAL:
                cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
                break;
            case PROTOCOL:
                NodeDriver.getInstance().getExecutor().sendPacketToAll(
                        PacketCloudPlayer.forPlayerUpdate(cloudPlayer)
                );
                break;
            case GLOBAL:
                updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
                updateCloudPlayer(cloudPlayer, PublishingType.PROTOCOL);

                break;
        }
    }

    @Override
    public void handle(PacketChannel wrapper, PacketCloudPlayer packet) {
        PacketBuffer buffer = packet.buffer();

        PacketCloudPlayer.PayLoad payLoad = buffer.readEnum(PacketCloudPlayer.PayLoad.class);

        switch (payLoad) {
            case PROXY_LOGIN_REQUEST:
                UUID uniqueId = buffer.readOptionalUniqueId();
                String name = buffer.readOptionalString();
                if (name == null) {
                    name = "UNKNOWN";
                }
                ICloudService possibleServer = wrapper.getPossibleServer();

                if (possibleServer == null) {
                    error("§cNo Server provided for Channel §e{}", wrapper);
                    return;
                }


                boolean maintenance = possibleServer.getTask().getVersion().isProxy() && possibleServer.getTask().isMaintenance();

                String cancelReason;

                Task<ICloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

                ICloudPlayer player = getCachedCloudPlayer(uniqueId);
                if (player != null) {

                    warn("Player[name={} uuid={}] is already connected to the network!", name, uniqueId);
                    cancelReason = "§cYou are already connected to the network";
                } else {
                    CloudOfflinePlayer offlinePlayer = getOfflinePlayer(uniqueId).syncUninterruptedly().orElse(null);
                    boolean firstJoin = false;
                    //if first time joining create database entry
                    if (offlinePlayer == null) {
                        firstJoin = true;
                        offlinePlayer = new DefaultCloudOfflinePlayer(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis(), Document.newJsonDocument());
                    }
                    if (maintenance && !offlinePlayer.hasPermission("cloud.maintenance.bypass")) {
                        warn("Player[name={} uuid={}] tried to log in but may not join whilst maintenance", name, uniqueId);
                        cancelReason = "§cThe network is in maintenance"; // TODO: 28.04.2025 fulljoin permission
                    } else if (CloudDriver.getInstance().getServiceTaskManager().countPlayerCapacity() > 0 && getCloudPlayerOnlineAmount() >= CloudDriver.getInstance().getServiceTaskManager().countPlayerCapacity() && !offlinePlayer.hasPermission("cloud.full.join")) {
                        warn("Player[name={} uuid={}] tried to log in but proxies are not capable of one more player!", name, uniqueId);
                        cancelReason = "§cAll proxies are full and you are not permitted to kick a player";
                    } else if (fallback.isNull()) {
                        debug("Player[name={} uuid={}] tried to log in but no fallback was found!", name, uniqueId);
                        cancelReason = "§cNo suitable fallback was found for you to connect to!";
                    } else {
                        debug("Player[name={} uuid={}] is allowed log in!", name, uniqueId);
                        player = UniversalCloudPlayer.fromOfflinePlayer(offlinePlayer);
                        if (firstJoin) {
                            offlinePlayer.saveOfflinePlayer();
                            CloudDriver.getInstance().getLogger().debug("Created DatabaseEntry for Player[name={}, uuid={}]", player.getName(), player.getUniqueId());
                            CloudDriver.getInstance().getEventManager().callEvent(new CloudPlayerLoginFirstTimeEvent(player), PublishingType.GLOBAL);

                        }
                        cancelReason = null;
                        registerPlayer(player); //register player if no kick reason was provided otherwise it would be stuck in cache
                    }
                }

                ICloudPlayer finalPlayer = player;
                if (cancelReason != null) {
                    packet.respond(NetworkResponseState.ERROR, buf -> buf.writeOptionalObject(finalPlayer).writeOptionalString(cancelReason));
                } else {
                    packet.respond(NetworkResponseState.OK, buf -> buf.writeOptionalObject(finalPlayer).writeOptionalString(null));
                }

                break;

            case PROXY_LOGIN_FAILED:
                UUID playerId = buffer.readOptionalUniqueId();
                String proxyName = buffer.readString();
                String reason = buffer.readString();
                if (playerId == null) {
                    return;
                }
                ICloudPlayer cloudPlayer = getCachedCloudPlayer(playerId);
                if (cloudPlayer == null) {
                    return;
                }
                debug("Player[name={} uuid={}] couldn't log in on '{}' because: {}", cloudPlayer.getName(), cloudPlayer.getUniqueId(), proxyName, reason);
                unregisterCloudPlayer(playerId, cloudPlayer.getName());
                break;

            case PLAYER_COMMAND_EXECUTE:
                UUID cmdPlayerID = buffer.readUniqueId();
                String commandLine = buffer.readString();

                ICloudPlayer player1 = getCachedCloudPlayer(cmdPlayerID);
                if (player1 != null) {
                    CloudDriver.getInstance().getLogger().debug("Player [name={}, uuid={}] executed CloudSided-Ingame-command: '{}'", player1.getName(), player1.getUniqueId(), commandLine);
                    CloudDriver.getInstance().getCommandManager().executeCommand(player1, commandLine);
                }
                break;
            case PROXY_LOGIN_SUCCESS:
                UUID id = buffer.readOptionalUniqueId();
                String proxy = buffer.readString();
                String firstJoin = buffer.readString();
                if (id == null) {
                    break;
                }

                ICloudService proxyS = CloudDriver.getInstance().getServiceManager().getCachedCloudService(proxy);
                ICloudService minecraftS = CloudDriver.getInstance().getServiceManager().getCachedCloudService(firstJoin);

                if (proxyS == null || minecraftS == null) {
                    error("Either proxy or MinecraftServer returned null from protocol!");
                    return;
                }

                ICloudPlayer cachedCloudPlayer = getCachedCloudPlayer(id);
                if (cachedCloudPlayer != null) {
                    cachedCloudPlayer.setProxyServer(proxyS);
                    cachedCloudPlayer.setServer(minecraftS);
                    cachedCloudPlayer.update(PublishingType.INTERNAL);
                    CloudDriver.getInstance().getEventManager().callEvent(new CloudPlayerLoginEvent(cachedCloudPlayer), PublishingType.GLOBAL);
                    debug("Player[name={} uuid={}] logged in [proxy={} server={}]", cachedCloudPlayer.getName(), cachedCloudPlayer.getUniqueId(), proxyS.getName(), minecraftS.getName());
                }
                //handle if connecting or connected to server
                break;
            case SERVER_CONNECTED_SUCCESS:
            case SERVER_CONNECTED:
                UUID uuid = buffer.readOptionalUniqueId();
                String serverName = buffer.readString();

                if (uuid == null) {
                    return;
                }

                ICloudPlayer iCloudPlayer = getCachedCloudPlayer(uuid);
                ICloudService cachedCloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(serverName);
                PlayerExecutor executor = PlayerExecutor.forProxy(uuid, wrapper.getPossibleServer());

                String kickReason = null;
                if (iCloudPlayer == null) {
                    kickReason = "§cYou are not allowed to be on the network without being registered in the Cloud!";
                }
                if (cachedCloudService == null) {
                    kickReason = "§cYou are not allowed to join a Server without it being registered in the Cloud!";
                }
                if (kickReason != null) {
                    executor.disconnect(kickReason);
                    return;
                }

                iCloudPlayer.setServer(cachedCloudService);
                iCloudPlayer.update(PublishingType.INTERNAL);

                if (payLoad == PacketCloudPlayer.PayLoad.SERVER_CONNECTED_SUCCESS) {
                    CloudDriver.getInstance().getEventManager().callEvent(new CloudPlayerChangeServerEvent(iCloudPlayer, cachedCloudService), PublishingType.GLOBAL);
                }
                break;

            case PLAYER_UPDATE:
                UniversalCloudPlayer universalCloudPlayer = buffer.readObject(UniversalCloudPlayer.class);
                this.updateInternal(universalCloudPlayer);

                CloudDriver.getInstance().getEventManager().callEvent(new CloudPlayerUpdateEvent(universalCloudPlayer), PublishingType.GLOBAL);
                break;

            case PROXY_PLAYER_DISCONNECT:
                UUID proxyPlayerId = buffer.readUniqueId();

                ICloudPlayer cp = this.getCachedCloudPlayer(proxyPlayerId);
                if (cp != null) {
                    this.unregisterCloudPlayer(cp.getUniqueId(), cp.getName());
                    packet.respond(NetworkResponseState.OK);
                } else {
                    packet.respond(NetworkResponseState.BAD_REQUEST);
                    warn("Tried to disconnect unknown CloudPlayer [UUID={}]", proxyPlayerId);
                }
                break;
        }
    }

    @Override
    public boolean hasJoinedTheNetworkBefore(UUID uniqueId, Consumer<CloudOfflinePlayer> handler) {
        return false; // TODO: 29.04.2025
    }
}
