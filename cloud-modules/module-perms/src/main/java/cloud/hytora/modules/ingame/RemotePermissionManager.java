package cloud.hytora.modules.ingame;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.message.IMessageChannel;
import cloud.hytora.driver.networking.packets.PacketRegistry;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.QueryResponse;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.modules.DefaultPermissionManager;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class RemotePermissionManager extends DefaultPermissionManager {

    private final List<PermissionGroup> allCachedPermissionGroups;
    private final List<PermissionPlayer> allCachedPermissionPlayers;

    private final IMessageChannel<>

    public RemotePermissionManager() {
        super();
        this.allCachedPermissionGroups = new ArrayList<>();
        this.allCachedPermissionPlayers = new ArrayList<>();

        //registering packets
        PacketRegistry.autoRegister(PermsGroupPacket.class);
        PacketRegistry.autoRegister(PermsPlayerRequestPacket.class);
        PacketRegistry.autoRegister(PermsPlayerUpdatePacket.class);
        PacketRegistry.autoRegister(PermsCacheUpdatePacket.class);

        //registering handler
        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<PermsGroupPacket>) (wrapper, packet) -> {
            PermissionGroup permissionGroups = packet.getGroup();
            switch (packet.getPayLoad()) {
                case UPDATE:

                    allCachedPermissionGroups.removeIf(g -> g.getName().equalsIgnoreCase(permissionGroups.getName()));
                    allCachedPermissionGroups.add(permissionGroups);
                    break;
                case CREATE:
                    allCachedPermissionGroups.add(permissionGroups);
                    break;
                case REMOVE:
                    allCachedPermissionGroups.removeIf(g -> g.getName().equalsIgnoreCase(permissionGroups.getName()));
                    break;
            }
        });
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new RemotePlayerUpdatePacketHandler(this));
    }

    @Nullable
    @Override
    public PermissionGroup getPermissionGroup(@NotNull String name) {
        return this.allCachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @NotNull
    @Override
    public Task<PermissionGroup> getPermissionGroupAsync(@NotNull String name) {
        return Task.callAsync(() -> getPermissionGroup(name));
    }

    @Override
    public void updatePermissionGroup(PermissionGroup group) {
        PermissionGroup oldGroup = this.getPermissionGroup(group.getName());
        if (oldGroup == null) {
            this.allCachedPermissionGroups.add(group);
        } else {
            int index = this.allCachedPermissionGroups.indexOf(oldGroup);
            this.allCachedPermissionGroups.set(index, group);
        }

        PermsGroupPacket packet = new PermsGroupPacket(PermsGroupPacket.PayLoad.UPDATE, group, group.getName());
        packet.publishAsync();
    }

    @Override
    public void addPermissionGroup(PermissionGroup group) {
        this.allCachedPermissionGroups.add(group);

        PermsGroupPacket packet = new PermsGroupPacket(PermsGroupPacket.PayLoad.CREATE, group, group.getName());
        packet.publishAsync();
    }

    @Override
    public void deletePermissionGroup(String name) {
        this.allCachedPermissionGroups.removeIf(group -> group.getName().equalsIgnoreCase(name));

        PermsGroupPacket packet = new PermsGroupPacket(PermsGroupPacket.PayLoad.REMOVE, null, name);
        packet.publishAsync();
    }

    @Nullable
    @Override
    public PermissionPlayer getPermissionPlayer(@NotNull UUID uniqueId) {
        return this.allCachedPermissionPlayers
                .stream()
                .filter(p -> p.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElseGet(() -> {
                    BufferedResponse response = new PermsPlayerRequestPacket(null, uniqueId)
                            .sendQuery()
                            .execute()
                            .syncUninterruptedly()
                            .get();
                    DefaultPermissionPlayer permissionPlayer = response
                            .buffer().readObject(DefaultPermissionPlayer.class);
                    addToCache(permissionPlayer);
                    return permissionPlayer;
                });
    }

    @Override
    public boolean hasEntry(UUID uniqueId) {
        QueryResponse response = Query.get().createRequest(CloudDriver.Constants.QUERY_CHANNEL_PLAYER)
                .setKey("perms_has_entry")
                .setBuffer(buffer -> buffer.writeUniqueId(uniqueId))
                .syncUninterruptedlyAndExecute();
        return response.getState().toBoolean();
    }

    @Nullable
    @Override
    public PermissionPlayer getPermissionPlayer(@NotNull String name) {
        return this.allCachedPermissionPlayers
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    BufferedResponse response = new PermsPlayerRequestPacket(name, null)
                            .sendQuery()
                            .execute()
                            .syncUninterruptedly()
                            .get();
                    DefaultPermissionPlayer permissionPlayer = response
                            .buffer().readObject(DefaultPermissionPlayer.class);
                    addToCache(permissionPlayer);
                    return permissionPlayer;
                });
    }


    @Override
    public void updatePermissionPlayer(PermissionPlayer player) {
        addToCache(player);

        PermsPlayerUpdatePacket packet = new PermsPlayerUpdatePacket(player);
        CloudDriver.getInstance().getExecutor().sendPacket(packet);
    }

    @Override
    public void addToCache(PermissionPlayer player) {

        this.allCachedPermissionPlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));

        this.allCachedPermissionPlayers.add(player);

        // TODO: 08.05.2025 check for more efficent method
        CloudDriver.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId())
                .onTaskSucess(offlinePlayer -> {
                    if (offlinePlayer.hasProperty("module_perms_highest_group")) {
                        return;
                    }
                    offlinePlayer.setProperty("module_perms_highest_group", player.getHighestGroup().getName());
                    offlinePlayer.save();
                });
    }

}
