package cloud.hytora.modules.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Getter @Setter
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

    public DefaultPermissionGroup() {
        this.permissions = new HashMap<>();
    }

    public DefaultPermissionGroup(String name, String color, String chatColor, String namePrefix, String prefix, String suffix, int sortId, boolean defaultGroup, Collection<String> inheritedGroups, Map<String, Long> permissions) {
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
                break;
        }
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
    public Task<Permission> getPermission(String permission) {
        return Task.callAsync(() -> {
            Long timeOut = permissions.get(permission);
            return timeOut == null ? null : Permission.of(permission, timeOut);
        });
    }

    @Override
    public Permission getPermissionOrNull(String permission) {
        Long timeOut = permissions.get(permission);
        return timeOut == null ? null : Permission.of(permission, timeOut);
    }

    @Override
    public boolean hasPermission(String permission) {
        Permission perm = this.getPermissionOrNull(permission);
        if (perm == null) {
            for (String groupName : getInheritedGroups()) { // TODO: 27.07.2022 check construct here
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
        if (perm.hasExpired()) { //permission has expired ==> removing it and updating
            this.removePermission(perm);
            this.update();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) { // TODO: 27.07.2022 check this

        if (permission.hasExpired()) { //permission has expired ==> removing it and updating
            this.removePermission(permission);
            this.update();
            return false;
        }
        if (this.getPermission(permission.getPermission()).isPresent()) {
            return true;
        } else {
            for (String groupName : getInheritedGroups()) { // TODO: 27.07.2022 check construct here
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
