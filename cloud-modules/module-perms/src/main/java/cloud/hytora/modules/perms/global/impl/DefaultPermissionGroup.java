package cloud.hytora.modules.perms.global.impl;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public class DefaultPermissionGroup implements PermissionGroup {

    /**
     * The name of this group
     */
    private String name;

    /**
     * THe color off this group
     */
    private String color;

    /**
     * The chat color of this group
     */
    private String chatColor;

    /**
     * THe prefix infront of a name
     */
    private String namePrefix;

    /**
     * The general prefix
     */
    private String prefix;

    /**
     * THe suffix of the group
     */
    private String suffix;

    /**
     * The id to sort groups after
     */
    private int sortId;

    /**
     * If this is a default group
     */
    private boolean defaultGroup;

    /**
     * THe inherited groups
     */
    private Collection<String> inheritedGroups;

    /**
     * The cached permissions of this group
     */
    private Map<String, Long> permissions;

    private Collection<String> deniedPermissions;

    private Map<String, Collection<String>> taskPermissions;

    public DefaultPermissionGroup() {
        this.permissions = new HashMap<>();
        this.deniedPermissions = new ArrayList<>();
        this.taskPermissions = new ConcurrentHashMap<>();
    }

    public DefaultPermissionGroup(String name, String color, String chatColor, String namePrefix, String prefix, String suffix, int sortId, boolean defaultGroup, Collection<String> inheritedGroups, Map<String, Long> permissions) {
        this();
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
        this.namePrefix = namePrefix;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sortId = sortId;
        this.defaultGroup = defaultGroup;
        this.inheritedGroups = inheritedGroups;
        this.permissions = permissions;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case WRITE:
                buf.writeString(name);
                buf.writeString(color);
                buf.writeString(chatColor);
                buf.writeString(namePrefix);
                buf.writeString(prefix);
                buf.writeString(suffix);
                buf.writeInt(sortId);
                buf.writeBoolean(defaultGroup);
                buf.writeStringCollection(inheritedGroups);
                buf.writeObjectCollection(this.getPermissions());
                buf.writeStringCollection(getDeniedPermissions());
                buf.writeMap(taskPermissions, PacketBuffer::writeString, PacketBuffer::writeStringCollection);
                break;

            case READ:
                name = buf.readString();
                color = buf.readString();
                chatColor = buf.readString();
                namePrefix = buf.readString();
                prefix = buf.readString();
                suffix = buf.readString();
                sortId = buf.readInt();
                defaultGroup = buf.readBoolean();
                inheritedGroups = buf.readStringCollection();
                permissions = new HashMap<>();
                for (DefaultPermission perm : buf.readObjectCollection(DefaultPermission.class)) {
                    this.addPermission(perm);
                }
                deniedPermissions = buf.readStringCollection();
                this.taskPermissions = buf.readMap(PacketBuffer::readString, PacketBuffer::readStringCollection);
                break;
        }
    }

    @Override
    public void setTaskPermissions(Map<IServiceTask, Collection<String>> taskPermissions) {
        for (Map.Entry<IServiceTask, Collection<String>> e : taskPermissions.entrySet()) {
            this.taskPermissions.put(e.getKey().getName(), e.getValue());
        }
    }

    @Override
    public Map<IServiceTask, Collection<String>> getTaskPermissions() {
        Map<IServiceTask, Collection<String>> taskPermissions = new ConcurrentHashMap<>();
        for (Map.Entry<String, Collection<String>> e : this.taskPermissions.entrySet()) {
            taskPermissions.put(CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getTaskOrNull(e.getKey()), e.getValue());
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
    public void addPermission(Permission permission) {
        this.permissions.put(permission.getPermission(), permission.getExpirationDate());
    }

    @Override
    public void removePermission(Permission permission) {
        this.permissions.remove(permission.getPermission());
    }

    @Override
    public Collection<Permission> getPermissions() {
        return this.permissions.keySet().stream().map(permission -> {
            long timeOut = permissions.get(permission);

            return Permission.of(permission, timeOut);
        }).collect(Collectors.toList());
    }

    @Override
    public IPromise<Permission> getPermission(String permission) {
        return IPromise.callAsync(() -> {
            Long timeOut = permissions.get(permission);
            return permissions.containsKey(permission) ? Permission.of(permission, timeOut) : null;
        });
    }

    @Override
    public Permission getPermissionOrNull(String permission) {
        Long timeOut = permissions.get(permission);
        return permissions.containsKey(permission) ? Permission.of(permission, timeOut) : null;
    }

    public void checkForExpiredValues() {
        if (testPerms()) {
            this.update();
        }
    }

    public boolean testPerms() {
        int sizeBefore = permissions.size();
        for (String permission : new ArrayList<>(permissions.keySet())) {
            Permission permissionOrNull = this.getPermissionOrNull(permission);
            if (permissionOrNull.hasExpired()) {
                permissions.remove(permission);
            }
        }
        if (sizeBefore != permissions.size()) CloudDriver.getInstance().getLogger().trace("Removed expired perms from {}", this.getName());
        return sizeBefore != permissions.size();
    }
    @Override
    public boolean hasPermission(String permission) {
        this.checkForExpiredValues();
        Permission perm = this.getPermissionOrNull(permission);
        if (perm == null) {
            for (PermissionGroup group : this.findInheritedGroups()) {
                if (group != null && group.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {

        if (permission.hasExpired()) { //permission has expired ==> removing it and updating
            this.removePermission(permission);
            this.update();
            return false;
        }
        if (this.getPermission(permission.getPermission()).isPresent()) {
            return true;
        } else {
            for (String groupName : getInheritedGroups()) {
                PermissionGroup group = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(groupName);
                if (group == null) {
                    continue;
                }
                if (group.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).updatePermissionGroup(this);
    }

    @Override
    public void addInheritedGroup(@NotNull String group) {
        this.inheritedGroups.add(group);
    }

    @Override
    public void removeInheritedGroup(@NotNull String group) {
        this.inheritedGroups.remove(group);
    }

    @Override
    public String getMainIdentity() {
        return name;
    }
}
