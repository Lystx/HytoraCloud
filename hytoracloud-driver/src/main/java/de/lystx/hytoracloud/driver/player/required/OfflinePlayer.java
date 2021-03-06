package de.lystx.hytoracloud.driver.player.required;

import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.connection.protocol.requests.base.DriverQuery;
import de.lystx.hytoracloud.driver.player.permission.impl.PermissionEntry;
import de.lystx.hytoracloud.driver.player.permission.impl.PermissionGroup;
import de.lystx.hytoracloud.driver.player.permission.impl.PermissionValidity;
import de.lystx.hytoracloud.driver.player.permission.impl.IPermissionUser;


import de.lystx.hytoracloud.driver.utils.json.PropertyObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

@Getter @Setter @AllArgsConstructor
public class OfflinePlayer implements Serializable, IPermissionUser {

    private static final long serialVersionUID = 4187696359635163423L;

    /**
     * The uuid of this player
     */
    private UUID uniqueId;

    /**
     * The name of this player
     */
    private String name;

    /**
     * The loaded permission entries
     */
    private List<PermissionEntry> permissionEntries;

    /**
     * Exclusive permissions
     */
    private List<String> permissions;

    /**
     * The ip address
     */
    private String ipAddress;

    /**
     * if server messages received
     */
    private boolean notifyServerStart;

    /**
     * If this is a default created
     */
    private boolean isDefault;

    /**
     * First login
     */
    private long firstLogin;

    /**
     * last login
     */
    private long lastLogin;

    /**
     * Properties
     */
    private Map<String, PropertyObject> properties;

    public OfflinePlayer(UUID uniqueId, String name, List<PermissionEntry> permissionEntries, List<String> permissions, String ipAddress, boolean notifyServerStart, long firstLogin, long lastLogin) {
        this(uniqueId, name, permissionEntries, permissions, ipAddress, notifyServerStart, false, firstLogin, lastLogin, new HashMap<>());
    }

    /**
     * Adds a property to this player
     *
     * @param name the name (e.g "global")
     * @param data the data (e.g. coins or something)
     */
    public void addProperty(String name, PropertyObject data) {
        this.properties.put(name, data);
    }

    /**
     * Searches for a property with a given name
     *
     * @param name the name of the property
     * @return jsonObject
     */
    public PropertyObject getProperty(String name) {
        return this.properties.get(name);
    }

    /**
     * Gets Entry
     * @param group the group
     * @return PermissionEntry from group (e.g. "Admin")
     */
    public PermissionEntry getPermissionEntryOfGroup(String group) {
        return this.permissionEntries.stream().filter(permissionEntry -> group.equalsIgnoreCase(permissionEntry.getPermissionGroup())).findFirst().orElse(null);
    }

    /**
     * Returns all the {@link PermissionGroup}s
     * @return list of groups
     */
    public List<PermissionGroup> getPermissionGroups() {
        List<PermissionGroup> list = new ArrayList<>();
        for (PermissionEntry permissionEntry : this.permissionEntries) {
            PermissionGroup permissionGroup = CloudDriver.getInstance().getPermissionPool().getPermissionGroupByName(permissionEntry.getPermissionGroup());
            if (permissionGroup != null) {
                list.add(permissionGroup);
            }
        }
        return list;
    }

    /**
     * Updates this data
     */
    public void update() {
        CloudDriver.getInstance().getPermissionPool().update(this);
        CloudDriver.getInstance().getPermissionPool().update();
        CloudDriver.getInstance().getPermissionPool().update();
    }

    @Override
    public boolean hasPermission(String permission) {
        return CloudDriver.getInstance().getPermissionPool().hasPermission(this.getUniqueId(), permission);
    }

    @Nullable
    @Override
    public PermissionGroup getCachedPermissionGroup() {
        return CloudDriver.getInstance().getPermissionPool().getHighestPermissionGroup(this.getUniqueId());
    }

    @Override
    public DriverQuery<PermissionGroup> getPermissionGroup() {
        return DriverQuery.dummy("PLAYER_GET_PERMISSIONGROUP", this.getCachedPermissionGroup());
    }

    @SneakyThrows
    @Override
    public DriverQuery<Boolean> addPermission(String permission) {
        CloudDriver.getInstance().getPermissionPool().addPermissionToUser(this.getUniqueId(), permission);
        CloudDriver.getInstance().getPermissionPool().update();
        return DriverQuery.dummy("PLAYER_ADD_PERMISSION", true);
    }

    @Override
    public DriverQuery<Boolean> removePermission(String permission) {
        CloudDriver.getInstance().getPermissionPool().removePermissionFromUser(this.getUniqueId(), permission);
        CloudDriver.getInstance().getPermissionPool().update();
        return DriverQuery.dummy("PLAYER_REMOVE_PERMISSION", true);
    }

    @Override
    public List<String> getExclusivePermissions() {
        return this.permissions;
    }

    @Override
    public List<PermissionGroup> getAllPermissionGroups() {
        return CloudDriver.getInstance().getPermissionPool().getPermissionGroups(this.getUniqueId());
    }

    @Override
    public DriverQuery<PermissionGroup> removePermissionGroup(PermissionGroup permissionGroup) {
        CloudDriver.getInstance().getPermissionPool().removePermissionGroupFromUser(this.getUniqueId(), permissionGroup);
        return DriverQuery.dummy("PLAYER_REMOVE_GROUP", permissionGroup);
    }

    @Override
    public DriverQuery<PermissionGroup> addPermissionGroup(PermissionGroup permissionGroup, int time, PermissionValidity unit) {
        CloudDriver.getInstance().getPermissionPool().addPermissionGroupToUser(this.getUniqueId(), permissionGroup, time, unit);
        return DriverQuery.dummy("PLAYER_ADD_GROUP", permissionGroup);
    }

}
