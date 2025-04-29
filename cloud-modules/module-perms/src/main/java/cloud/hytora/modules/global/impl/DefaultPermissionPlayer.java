package cloud.hytora.modules.global.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.task.IServiceTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Setter
public class DefaultPermissionPlayer implements PermissionPlayer {

    private String name;
    private UUID uniqueId;
    public Map<String, Long> permissions; //<Name, Instance>

    public Map<String, Long> groups; //<Name, TimeOut>

    @Setter
    public Collection<String> deniedPermissions;

    public Map<String, Collection<String>> taskPermissions;

    public DefaultPermissionPlayer(String name, UUID uniqueId) {
        this.name = name;
        this.uniqueId = uniqueId;

        this.permissions = new HashMap<>();
        this.groups = new HashMap<>();

        this.deniedPermissions = new ArrayList<>();
        this.taskPermissions = new ConcurrentHashMap<>();
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
                this.groups = buf.readMap(PacketBuffer::readString, PacketBuffer::readLong);
                this.deniedPermissions = buf.readStringCollection();
                this.taskPermissions = buf.readMap(PacketBuffer::readString, PacketBuffer::readStringCollection);
                break;

            case WRITE:
                buf.writeString(name);
                buf.writeUniqueId(uniqueId);
                buf.writeObjectCollection(this.getPermissions());
                buf.writeMap(groups, PacketBuffer::writeString, PacketBuffer::writeLong);
                buf.writeStringCollection(getDeniedPermissions());
                buf.writeMap(taskPermissions, PacketBuffer::writeString, PacketBuffer::writeStringCollection);
                break;
        }
    }

    @Override
    public Map<IServiceTask, Collection<String>> getTaskPermissions() {
        Map<IServiceTask, Collection<String>> taskPermissions = new ConcurrentHashMap<>();
        for (Map.Entry<String, Collection<String>> e : this.taskPermissions.entrySet()) {
            taskPermissions.put(CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(e.getKey()), e.getValue());
        }
        return taskPermissions;
    }

    @Override
    public void addDeniedPermission(String permission) {
        this.deniedPermissions.add(permission);
    }

    @Override
    public void removeDeniedPermission(String permission) {
        this.deniedPermissions.remove(permission);
    }

    @Override
    public void addTaskPermission(IServiceTask task, String permission) {
        Collection<String> taskPermissions = this.getTaskPermissions(task.getName());
        if (!taskPermissions.contains(permission)) {
            taskPermissions.add(permission);
        }
        this.taskPermissions.put(task.getName(), taskPermissions);
    }

    @Override
    public void removeTaskPermission(IServiceTask task, String permission) {

        Collection<String> taskPermissions = this.getTaskPermissions(task.getName());
        taskPermissions.remove(permission);

        this.taskPermissions.put(task.getName(), taskPermissions);
    }

    @Override
    public void setTaskPermissions(Map<IServiceTask, Collection<String>> taskPermissions) {
        for (Map.Entry<IServiceTask, Collection<String>> e : taskPermissions.entrySet()) {
            this.taskPermissions.put(e.getKey().getName(), e.getValue());
        }
    }

    public void setTP(Map<String, Collection<String>> tp) {
        this.taskPermissions = tp;
    }

    @Override
    public void checkForExpiredValues() {
        boolean modifiedGroups = this.hasRemovedGroups();
        boolean modifiedPerms = this.hasRemovedPerms();

        if (modifiedGroups || modifiedPerms) {
            this.update();
        }
    }

    public boolean hasRemovedGroups() {
        if (groups == null || groups.keySet() == null) {
            return false;
        }

        Map<String, Long> permissionGroups = new HashMap<>(this.groups);
        long currentTime = System.currentTimeMillis();
        int sizeBefore = permissionGroups.size();

        for (String groupName : permissionGroups.keySet()) {
            if (permissionGroups.get(groupName) == null) {
                continue;
            }
            long timeOut = permissionGroups.get(groupName);

            if (timeOut == -1 ) {
                continue;
            }
            boolean groupNotFound = CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionGroupByNameOrNull(groupName) == null;
            boolean timedOut = currentTime > timeOut;

            if (timedOut || groupNotFound) {
                CloudDriver.getInstance().getLogger().info("Removed expired group '{}' from {}", groupName, this.getName());
                if (timedOut) {
                    CloudDriver.getInstance().getLogger().info("==> Group '{}' has timedOut [TimedOut:{}]", groupName, timeOut);
                } else {
                    CloudDriver.getInstance().getLogger().info("==> Group '{}' could not be found. Listed Groups: [{}]", groupName, CloudDriver.getInstance().getProvider(PermissionManager.class).getAllCachedPermissionGroups().stream().map(PermissionGroup::getName).collect(Collectors.toList()));
                }
                groups.remove(groupName);
            }
        }
        if (sizeBefore != groups.size())
            CloudDriver.getInstance().getLogger().info("Removed expired groups from {}", this.getName());
        return sizeBefore != groups.size();
    }


    public boolean hasRemovedPerms() {
        int sizeBefore = permissions.size();
        for (String permission : permissions.keySet()) {
            Permission dp = Permission.of(permission, permissions.get(permission));
            if (dp.hasExpired()) {
                CloudDriver.getInstance().getLogger().info("Removed expired permission '{}' from {}", dp.getPermission(), this.getName());

                permissions.remove(permission);
            }
        }
        if (sizeBefore != permissions.size())
            CloudDriver.getInstance().getLogger().info("Removed expired perms from {}", this.getName());
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
            for (PermissionGroup group : getPermissionGroups()) {
                if (group.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {

        this.checkForExpiredValues();
        if (this.getPermission(permission.getPermission()).isPresent()) {
            return true;
        } else {
            for (PermissionGroup group : getPermissionGroups()) {
                if (group.hasPermission(permission) && !group.getDeniedPermissions().contains(permission.getPermission())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getProvider(PermissionManager.class).updatePermissionPlayer(this);
    }

    @Nullable
    @Override
    public ICloudPlayer toOnlinePlayer() {
        return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(uniqueId);
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getPermissionGroups() {
        this.checkForExpiredValues();
        return this.groups.keySet().stream().map(s -> {
            PermissionGroup permissionGroup = CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionGroupByNameOrNull(s);
            if (permissionGroup == null) {
                System.out.println("No permissionGroup found by Name " + s);
            }
            return permissionGroup;
        }).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public PermissionGroup getHighestGroup() {
        this.checkForExpiredValues();
        Collection<PermissionGroup> permissionGroups = getPermissionGroups();
        if (permissionGroups.isEmpty()) {
            return null;
        }
        PermissionGroup group = null;
        for (PermissionGroup permissionGroup : permissionGroups) {
            if (permissionGroup == null) {
                continue;
            }
            if (group == null) {
                group = permissionGroup;
            }
            if (permissionGroup.getSortId() < group.getSortId()) {
                group = permissionGroup;
            }
        }
        return group;
        /*
        ArrayList<PermissionGroup> wrapped = new ArrayList<>(permissionGroups);
        return wrapped.stream().min(Comparator.comparingInt(new ToIntFunction<PermissionGroup>() {
            @Override
            public int applyAsInt(PermissionGroup value) {
                if (value == null) {
                    return 99999;
                }
                return Integer.valueOf(value.getSortId());
            }
        })).orElse(null);*/
        //return wrapped.stream().min(Comparator.comparingInt(PermissionGroup::getSortId)).orElse(null);
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
