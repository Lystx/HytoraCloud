package cloud.hytora.modules.ingame;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.DefaultPermissionManager;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsGroupPacket;
import cloud.hytora.modules.global.packets.PermsPlayerRequestPacket;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class RemotePermissionManager extends DefaultPermissionManager implements PacketHandler<PermsCacheUpdatePacket> {

    private final List<PermissionGroup> allCachedPermissionGroups;
    private final List<PermissionPlayer> allCachedPermissionPlayers;

    public RemotePermissionManager() {
        this.allCachedPermissionGroups = new ArrayList<>();
        this.allCachedPermissionPlayers = new ArrayList<>();

        //registering packets
        PacketProvider.autoRegister(PermsCacheUpdatePacket.class);
        PacketProvider.autoRegister(PermsGroupPacket.class);
        PacketProvider.autoRegister(PermsPlayerRequestPacket.class);
        PacketProvider.autoRegister(PermsPlayerUpdatePacket.class);

        //registering handler
        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
    }

    @Nullable
    @Override
    public PermissionGroup getPermissionGroupByNameOrNull(@NotNull String name) {
        return this.allCachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @NotNull
    @Override
    public Task<PermissionGroup> getPermissionGroup(@NotNull String name) {
        return Task.callAsync(() -> getPermissionGroupByNameOrNull(name));
    }

    @Override
    public void updatePermissionGroup(PermissionGroup group) {
        PermissionGroup oldGroup = this.getPermissionGroupByNameOrNull(group.getName());
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
    public PermissionPlayer getPlayerByUniqueIdOrNull(@NotNull UUID uniqueId) {
        return this.allCachedPermissionPlayers.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElseGet(() -> {
            return new PermsPlayerRequestPacket(null, uniqueId).publishAsQuery().syncUninterruptedly().get().buffer().readObject(DefaultPermissionPlayer.class);
        });
    }


    @Nullable
    @Override
    public PermissionPlayer getPlayerByNameOrNull(@NotNull String name) {
        return this.allCachedPermissionPlayers.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElseGet(() -> {
            return new PermsPlayerRequestPacket(name, null).publishAsQuery().syncUninterruptedly().get().buffer().readObject(DefaultPermissionPlayer.class);
        });
    }

    @Override
    public Task<PermissionPlayer> getPlayerAsyncByUniqueId(UUID uniqueId) {
        Task<PermissionPlayer> task = Task.empty();

        PermissionPlayer player = this.allCachedPermissionPlayers.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
        if (player == null) {
            PermsPlayerRequestPacket packet = new PermsPlayerRequestPacket(null, uniqueId);
            packet.publishAsQuery().thenAccept(bufferedResponse -> {
                DefaultPermissionPlayer defaultPermissionPlayer = bufferedResponse.buffer().readObject(DefaultPermissionPlayer.class);
                task.setResult(defaultPermissionPlayer);
            });
        } else {
            task.setResult(player);
        }
        return task;
    }

    @Override
    public Task<PermissionPlayer> getPlayerAsyncByName(String name) {
        Task<PermissionPlayer> task = Task.empty();

        PermissionPlayer player = this.allCachedPermissionPlayers.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (player == null) {
             PermsPlayerRequestPacket packet = new PermsPlayerRequestPacket(name, null);
             packet.publishAsQuery().thenAccept(bufferedResponse -> {
                 DefaultPermissionPlayer defaultPermissionPlayer = bufferedResponse.buffer().readObject(DefaultPermissionPlayer.class);
                 task.setResult(defaultPermissionPlayer);
             });
        } else {
            task.setResult(player);
        }
        return task;
    }

    @Override
    public void updatePermissionPlayer(PermissionPlayer player) {
        Packet packet = new PermsPlayerUpdatePacket(player);
        packet.publishAsync();
    }

    @Override
    public void handle(PacketChannel wrapper, PermsCacheUpdatePacket packet) {
        Collection<PermissionGroup> permissionGroups = packet.getPermissionGroups();
        Collection<PermissionPlayer> permissionPlayers = packet.getPermissionPlayers();

        this.allCachedPermissionPlayers.clear();
        this.allCachedPermissionGroups.clear();

        this.allCachedPermissionPlayers.addAll(permissionPlayers);
        this.allCachedPermissionGroups.addAll(permissionGroups);
    }
}
