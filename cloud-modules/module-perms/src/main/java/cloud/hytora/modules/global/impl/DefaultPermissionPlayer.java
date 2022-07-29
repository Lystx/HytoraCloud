package cloud.hytora.modules.global.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class DefaultPermissionPlayer implements PermissionPlayer {

    private String name;
    private UUID uniqueId;
    private Map<String, Long> permissions; //<Name, Instance>

    private Map<String, Long> groups; //<Name, TimeOut>

    public DefaultPermissionPlayer(String name, UUID uniqueId) {
        this.name = name;
        this.uniqueId = uniqueId;

        this.permissions = new HashMap<>();
        this.groups =  new HashMap<>();

    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {
            case READ:
                name = buf.readString();
                uniqueId = buf.readUniqueId();

                permissions = new HashMap<>();
                for (DefaultPermission defaultPermission : buf.readObjectCollection(DefaultPermission.class)) {
                    addPermission(defaultPermission);
                }
                int size = buf.readInt();
                this.groups = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String name = buf.readString();
                    long expirationDate = buf.readLong();
                    this.groups.put(name, expirationDate);
                }
                break;

            case WRITE:
                buf.writeString(name);
                buf.writeUniqueId(uniqueId);
                buf.writeObjectCollection(this.getPermissions());

                buf.writeInt(groups.size());
                for (String name : groups.keySet()) {
                    buf.writeString(name);
                    buf.writeLong(groups.get(name));
                }

                break;
        }
    }

    @Override
    public void checkForExpiredValues() {
        boolean modifiedGroups = this.testGroups();
        boolean modifiedPerms = this.testPerms();

        if (modifiedGroups || modifiedPerms) {
            this.update();
        }
    }

    public boolean testGroups() {
        long currentTime = System.currentTimeMillis();
        int sizeBefore = groups.size();
        for (String groupName : groups.keySet()) {
            long timeOut = groups.get(groupName);
            if (timeOut != -1 && currentTime > timeOut || CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(groupName) == null) {
                groups.remove(groupName);
            }
        }
        if (sizeBefore != groups.size()) CloudDriver.getInstance().getLogger().trace("Removed expired groups from {}", this.getName());
        return sizeBefore != groups.size();
    }


    public boolean testPerms() {
        int sizeBefore = permissions.size();
        for (String permission : permissions.keySet()) {
            Permission dp = getPermissionOrNull(permission);
            if (dp.hasExpired()) {
                permissions.remove(permission);
            }
        }
        if (sizeBefore != permissions.size()) CloudDriver.getInstance().getLogger().trace("Removed expired perms from {}", this.getName());
        return sizeBefore != permissions.size();
    }


    @Override
    public void addPermission(Permission permission) {
        this.permissions.put(permission.getPermission(), permission.getExpirationDate());
    }

    @Override
    public void removePermission(Permission permission) {
        this.permissions.remove(permission.getPermission());
    }

    @Override
    public Collection<Permission> getPermissions() {
        this.checkForExpiredValues();
        return this.permissions.keySet().stream().map(p -> Permission.of(p, this.permissions.get(p))).collect(Collectors.toList());
    }

    @Override
    public Task<Permission> getPermission(String permission) {
        this.checkForExpiredValues();
        return Task.callAsync(() -> this.permissions.containsKey(permission) ? Permission.of(permission, this.permissions.getOrDefault(permission, -1L)) : null);
    }

    @Override
    public Permission getPermissionOrNull(String permission) {
        this.checkForExpiredValues();
        return this.permissions.containsKey(permission) ? Permission.of(permission, this.permissions.getOrDefault(permission, -1L)) : null;
    }

    @Override
    public boolean hasPermission(String permission) {
        this.checkForExpiredValues();
        Permission perm = this.getPermissionOrNull(permission);
        if (perm == null) {
            for (PermissionGroup group : getPermissionGroups()) { // TODO: 27.07.2022 check construct here
                if (group.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
        if (perm.hasExpired()) { //permission has expired ==> removing it and updating
            this.removePermission(perm);
            this.update();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) { // TODO: 27.07.2022 check this

        this.checkForExpiredValues();
        if (permission.hasExpired()) { //permission has expired ==> removing it and updating
            this.removePermission(permission);
            this.update();
            return false;
        }
        if (this.getPermission(permission.getPermission()).isPresent()) {
            return true;
        } else {
            for (PermissionGroup group : getPermissionGroups()) { // TODO: 27.07.2022 check construct here
                if (group.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
    }
    @Override
    public void update() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).updatePermissionPlayer(this);
    }

    @Nullable
    @Override
    public CloudPlayer toOnlinePlayer() {
        return CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(uniqueId);
    }

    @NotNull
    @Override
    public CloudOfflinePlayer toOfflinePlayer() {
        return CloudDriver.getInstance().getPlayerManager().getOfflinePlayerByUniqueIdBlockingOrNull(uniqueId);
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getPermissionGroups() {
        this.checkForExpiredValues();
        return this.groups.keySet().stream().map(s -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(s)).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public PermissionGroup getHighestGroup() {
        this.checkForExpiredValues();
        return getPermissionGroups().stream().min(Comparator.comparingInt(PermissionGroup::getSortId)).orElse(null);
    }

    @Override
    public boolean isInPermissionGroup(String name) {
        this.checkForExpiredValues();
        return groups.get(name) != null;
    }

    @Override
    public void addPermissionGroup(@NotNull PermissionGroup group) {
        this.groups.put(group.getName(), -1L);
    }

    @Override
    public void addPermissionGroup(@NotNull PermissionGroup group, TimeUnit unit, long value) {
        this.groups.put(group.getName(), (System.currentTimeMillis() + unit.toMillis(value)));
    }

    @Override
    public void removePermissionGroup(String groupName) {
        this.groups.remove(groupName);
    }
}
