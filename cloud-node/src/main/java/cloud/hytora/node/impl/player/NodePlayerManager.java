package cloud.hytora.node.impl.player;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.command.sender.defaults.DefaultPlayerCommandSender;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.player.*;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityOfflinePlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.QueryState;
import cloud.hytora.driver.entity.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.player.extension.NodeBukkitPlayer;
import cloud.hytora.node.impl.player.extension.NodeProxyPlayer;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class NodePlayerManager extends DefaultPlayerManager implements PacketHandler<PacketCloudEntityPlayer>, PlayerExtension {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);

        HandlingNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler(this);

        CloudDriver.getInstance().setProvider(PlayerExtension.class, this);
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
                        player.handleJsonOperation(BufferState.READ, doc);
                        players.add(player);
                        cachedOfflinePlayers.put(player.getUniqueId(), player);
                    });
                    task.setResult(players);
                }).onTaskFailed(task::setFailure);


        return task;

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
                        cachedOfflinePlayers.put(player.getUniqueId(), player);
                        task.setResult(player);
                    });
                }).onTaskFailed(task::setFailure);
        return task;
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
                        cachedOfflinePlayers.put(player.getUniqueId(), player);
                        task.setResult(player);
                    });
                }).onTaskFailed(task::setFailure);


        return task;
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

            Task.runAsync(() -> new PacketCloudEntityOfflinePlayer(player, true).publishToAll());
            cachedOfflinePlayers.put(player.getUniqueId(), player);
            Logger.constantInstance().debug("Saving OfflinePlayer[name={}, uuid={}]", player.getName(), player.getUniqueId());
        });
    }

    @Override
    public Task<CloudPlayer> constructPlayer(@NotNull UUID uniqueId, @NotNull String name) {
        Task<CloudPlayer> task = Task.empty();
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
        CloudPlayer cp = getCachedCloudPlayer(uuid);
        if (cp == null) {
            return;
        }

        this.cachedCloudPlayers.remove(cp.getUniqueId());
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerDisconnect(cp), PublishingType.GLOBAL);
        CloudDriver.getInstance().getLogger().debug("Player[name={}, uuid={}] dissconnected from [proxy={}, server={}]!", cp.getName(), cp.getUniqueId(), cp.getProxyServer().getName(), (cp.getServer() == null ? "none" : cp.getServer().getName()));
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer, PublishingType... type) {
        //Update cache of every component
        PublishingType publishingType = PublishingType.get(type);
        switch (publishingType) {
            case INTERNAL:

                CloudPlayer cachedCloudPlayer = this.getCachedCloudPlayer(cloudPlayer.getUniqueId());
                if (cachedCloudPlayer != null) {
                    cachedCloudPlayer.clone(cloudPlayer);
                    cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cachedCloudPlayer);
                } else {
                    cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
                }
                cachedCloudPlayers.put(cloudPlayer.getUniqueId(), cloudPlayer);
                break;
            case PROTOCOL:
                NodeDriver.getInstance().getExecutor().sendPacketToAll(
                        PacketCloudEntityPlayer.forPlayerUpdate(cloudPlayer)
                );
                break;
            case GLOBAL:
                updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
                updateCloudPlayer(cloudPlayer, PublishingType.PROTOCOL);

                break;
        }
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerUpdate(cloudPlayer), PublishingType.GLOBAL);

    }

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityPlayer packet) {
        PacketBuffer buffer = packet.buffer();

        PacketCloudEntityPlayer.PayLoad payLoad = packet.getPayLoad();

        INetworkConfig config = CloudDriver.getInstance().getConfigManager().getConfig();
        UniversalCloudMessages messages = config.getMessages();

        switch (payLoad) {
            case PROXY_LOGIN_REQUEST:
                PlayerConnection connection = buffer.readObject(DefaultPlayerConnection.class);
                String firstJoinServerName = buffer.readString();
                if (connection.getConnectionName() == null) {
                    connection.setConnectionName("UNKNOWN");
                }

                String name = connection.getConnectionName();
                UUID uniqueId = connection.getConnectionId();


                CloudEventPlayerLogin loginEvent = CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerLogin(connection, CloudDriver.getInstance().getServiceManager().getCachedCloudService(firstJoinServerName)), PublishingType.INTERNAL);

                CloudService possibleServer = channel.getPossibleServer();

                if (possibleServer == null) {
                    error("§cNo Server provided for Channel §e{}", channel);
                    return;
                }


                boolean maintenance = possibleServer.getTask().getVersion().isProxy() && possibleServer.getTask().isMaintenance();

                String cancelReason;

                Task<CloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();

                CloudPlayer player = getCachedCloudPlayer(connection.getConnectionId());
                if (player != null) {

                    warn("Player[name={} uuid={}] is already connected to the network!", connection.getConnectionName(), connection.getConnectionId());

                    cancelReason = messages.getPrefix() + messages.getAlreadyOnNetworkMessage();
                } else {
                    CloudOfflinePlayer offlinePlayer = getOfflinePlayer(uniqueId).syncUninterruptedly().orElse(null);
                    boolean firstJoin = false;
                    //if first time joining create database entry
                    if (offlinePlayer == null) {
                        firstJoin = true;
                        offlinePlayer = new DefaultCloudOfflinePlayer(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis());
                    }

                    //setting proxy and minecraft server values
                    player = UniversalCloudPlayer.fromOfflinePlayer(offlinePlayer, connection.getProxyName(), firstJoinServerName);

                    if (maintenance && !offlinePlayer.hasPermission("cloud.maintenance.bypass")) {
                        warn("Player[name={} uuid={}] tried to log in but may not join whilst maintenance", name, uniqueId);
                        cancelReason = messages.getPrefix() + messages.getNetworkCurrentlyInMaintenance();
                    } else if (CloudDriver.getInstance().getServiceTaskManager().countProxyPlayerCapacity() > 0 && getCloudPlayerOnlineAmount() >= CloudDriver.getInstance().getServiceTaskManager().countProxyPlayerCapacity() && !offlinePlayer.hasPermission("cloud.full.join")) {
                        warn("Player[name={} uuid={}] tried to log in but proxies are not capable of one more player!", name, uniqueId);
                        cancelReason = messages.getPrefix() + messages.getAllProxiesFull();
                    } else if (fallback.isNull()) {
                        debug("Player[name={} uuid={}] tried to log in but no fallback was found!", name, uniqueId);
                        cancelReason = ComponentColor.translateAlternateColorCodes('&', messages.getPrefix() + messages.getNoAvailableFallbackMessage());
                    } else {
                        debug("Player[name={} uuid={}] is allowed log in!", name, uniqueId);
                        if (firstJoin) {
                            offlinePlayer.save();
                            CloudDriver.getInstance().getLogger().debug("Created DatabaseEntry for Player[name={}, uuid={}]", player.getName(), player.getUniqueId());
                            CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerLoginFirstTime(player), PublishingType.GLOBAL);

                        }
                        cancelReason = null;
                        player.setLastLogin(System.currentTimeMillis());
                        registerPlayer(player); //register player if no kick reason was provided otherwise it would be stuck in cache

                        player.update(PublishingType.GLOBAL); //sending player to every cache

                        String finalName = name;
                        CloudPlayer finalPlayer1 = player;
                        Query.get().createRequest(CloudDriver.Constants.QUERY_CHANNEL_PLAYER)
                                .setKey(CloudDriver.Constants.QUERY_KEY_PLAYER_CHECK_LOGIN)
                                .setBuffer(buf -> buf.writeUniqueId(uniqueId))
                                .executeDelayed(100) //equals 5 secs (5 * 20 ticks)
                                .onTaskSucess(response -> {
                                    QueryState state = response.getState();
                                    String message = response.getBuffer().readOptionalString();
                                    if (state == QueryState.SUCCESS) {
                                        //player did not timeOut in login
                                        return;
                                    }
                                    finalPlayer1.asProxyPlayer().disconnect(
                                            CloudDriver.getInstance()
                                                    .getConfigManager()
                                                    .getConfig()
                                                    .getMessages()
                                                    .getPrefix() + "§cYour login took to long. Please try again!"
                                    );
                                    error("Player[name={}, uuid={}] took too long to login. Had to kick player. Message => ", finalName, uniqueId, message);
                                });
                    }
                }

                CloudPlayer finalPlayer = player;
                if (cancelReason != null) {
                    packet.sendResponse()
                            .setState(NetworkResponseState.ERROR)
                            .setBuffer(buf -> {
                                buf.writeOptionalObject(finalPlayer);
                                buf.writeOptionalString(cancelReason);
                            })
                            .execute();
                } else {
                    packet.sendResponse()
                            .setState(NetworkResponseState.OK)
                            .setBuffer(buf -> {
                                buf.writeOptionalObject(finalPlayer);
                                buf.writeOptionalString(null);
                            })
                            .execute();
                }

                break;

            case PROXY_LOGIN_FAILED:
                UUID playerId = buffer.readOptionalUniqueId();
                String proxyName = buffer.readString();
                String reason = buffer.readString();
                if (playerId == null) {
                    return;
                }
                CloudPlayer cloudPlayer = getCachedCloudPlayer(playerId);
                if (cloudPlayer == null) {
                    return;
                }
                debug("Player[name={} uuid={}] couldn't log in on '{}' because: {}", cloudPlayer.getName(), cloudPlayer.getUniqueId(), proxyName, reason);
                unregisterCloudPlayer(playerId, cloudPlayer.getName());
                break;

            case PLAYER_COMMAND_EXECUTE:
                UUID cmdPlayerID = buffer.readUniqueId();
                String commandLine = buffer.readString();

                CloudPlayer player1 = getCachedCloudPlayer(cmdPlayerID);
                if (player1 != null) {
                    CloudDriver.getInstance().getCommandManager().executeCommand(player1, commandLine, true);
                    CloudDriver.getInstance().getLogger().debug("Player [name={}, uuid={}] executed CloudSided-Ingame-command: '{}'", player1.getName(), player1.getUniqueId(), commandLine);
                } else {
                    CloudDriver.getInstance().getLogger().warn("§cTried to execute command for nulled player with ID §e{}", cmdPlayerID);
                }
                break;
            case PROXY_LOGIN_SUCCESS:
                UUID id = buffer.readOptionalUniqueId();
                String proxy = buffer.readString();
                String firstJoin = buffer.readString();
                DefaultPlayerConnection playerConnection = buffer.readObject(DefaultPlayerConnection.class);
                if (id == null) {
                    break;
                }

                CloudService proxyS = CloudDriver.getInstance().getServiceManager().getCachedCloudService(proxy);
                CloudService minecraftS = CloudDriver.getInstance().getServiceManager().getCachedCloudService(firstJoin);

                if (proxyS == null || minecraftS == null) {
                    error("Either proxy or MinecraftServer returned null from protocol!");
                    return;
                }

                CloudPlayer cachedCloudPlayer = getCachedCloudPlayer(id);
                if (cachedCloudPlayer != null) {
                    cachedCloudPlayer.setProxyServer(proxyS);
                    cachedCloudPlayer.setServer(minecraftS);
                    cachedCloudPlayer.update(PublishingType.INTERNAL);
                    cachedCloudPlayer.setConnection(playerConnection);
                    CloudDriver.getInstance().getEventManager().callEvent(DefaultPlayerEvent.forLogin(cachedCloudPlayer), PublishingType.GLOBAL);
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

                CloudPlayer iCloudPlayer = getCachedCloudPlayer(uuid);
                CloudService cachedCloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(serverName);

                String kickReason = null;
                if (iCloudPlayer == null) {
                    kickReason = "§cYou are not allowed to be on the network without being registered in the Cloud!";
                }
                if (cachedCloudService == null) {
                    kickReason = "§cYou are not allowed to join a Server without it being registered in the Cloud!";
                }
                if (iCloudPlayer == null) {
                    break;
                }
                if (kickReason != null) {
                    iCloudPlayer.asProxyPlayer().disconnect(kickReason);
                    return;
                }

                iCloudPlayer.setServer(cachedCloudService);
                iCloudPlayer.update(PublishingType.INTERNAL);

                if (payLoad == PacketCloudEntityPlayer.PayLoad.SERVER_CONNECTED_SUCCESS) {
                    CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerChangeServer(iCloudPlayer, cachedCloudService), PublishingType.GLOBAL);
                }
                break;

            case PLAYER_TAB_COMPLETE:
                UUID senderUniqueId = buffer.readUniqueId();
                String command = buffer.readString();

                debug("CommandSystemPayload.{} -> {} '{}'", "PLAYER_TAB_COMPLETE", senderUniqueId, command);

                PlayerCommandSender sender = new DefaultPlayerCommandSender(CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(senderUniqueId));
                Collection<String> result = CloudDriver.getInstance().getCommandManager().completeCommand(sender, command);

                packet.sendResponse()
                        .setState(NetworkResponseState.OK)
                        .setBuffer(buf -> {
                            buf.writeStringCollection(result);
                        })
                        .execute();
                break;

            case PLAYER_UPDATE:
                UniversalCloudPlayer universalCloudPlayer = buffer.readObject(UniversalCloudPlayer.class);
                this.updateCloudPlayer(universalCloudPlayer, PublishingType.INTERNAL);

                break;

            case PROXY_PLAYER_DISCONNECT:
                UUID proxyPlayerId = buffer.readUniqueId();

                CloudPlayer cp = this.getCachedCloudPlayer(proxyPlayerId);
                if (cp != null) {
                    this.unregisterCloudPlayer(cp.getUniqueId(), cp.getName());

                    packet.sendResponse().setState(NetworkResponseState.OK).execute();
                } else {
                    packet.sendResponse().setState(NetworkResponseState.BAD_REQUEST).setError(new NullPointerException("No such player")).execute();
                    warn("Tried to disconnect unknown CloudPlayer [UUID={}]", proxyPlayerId);
                }
                break;
        }
    }

    @Override
    public CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer) {
        return new NodeProxyPlayer(cloudPlayer);
    }

    @Override
    public CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer) {
        return new NodeBukkitPlayer(cloudPlayer);

    }
}