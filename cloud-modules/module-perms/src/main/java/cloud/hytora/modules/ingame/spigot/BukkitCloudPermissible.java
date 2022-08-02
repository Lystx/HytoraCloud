package cloud.hytora.modules.ingame.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.services.ICloudServer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class BukkitCloudPermissible extends PermissibleBase {

    /**
     * The player for this base
     */
    private final Player player;

    /**
     * The failed tries
     */
    private int tries;

    /**
     * The cached perms
     */
    private Map<String, PermissionAttachmentInfo> perms;

    private PermissionPlayer permissionPlayer;

    public BukkitCloudPermissible(Player player) {
        super(player);
        this.perms = new HashMap<>();
        this.player = player;
        this.tries = 0;

        player.setOp(false);
        this.recalculatePermissions();
    }

    @Override
    public boolean isOp() {
        return this.hasPermission("*");
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.hasPermission(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.hasPermission(perm.getName());
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>(perms.values());
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.hasPermission(perm.getName());
    }

    @Override
    public boolean hasPermission(@NotNull String inName) {
        return getPermissionPlayer().hasPermission("*") || getPermissionPlayer().hasPermission(inName);
    }

    @Override
    public void recalculatePermissions() {
        this.perms = new HashMap<>();
        if (player == null || Bukkit.getPlayer(this.player.getName()) == null) {
            return;
        }
        try {


            if (getPermissionPlayer() == null) return;

            for (cloud.hytora.driver.permission.Permission permission : permissionPlayer.getPermissions()) {
                perms.put(permission.getPermission(), new PermissionAttachmentInfo(this, permission.getPermission(), null, true));
            }

            for (String taskPermission : permissionPlayer.getTaskPermissions(CloudDriver.getInstance().thisSidesClusterParticipant(ICloudServer.class).getTask().getName())) {
                perms.put(taskPermission, new PermissionAttachmentInfo(this, taskPermission, null, true));
            }

            //denied permissions
            for (String permission : permissionPlayer.getDeniedPermissions()) {
                perms.put(permission, new PermissionAttachmentInfo(this, permission, null, false));
            }

            for (PermissionGroup group : permissionPlayer.getPermissionGroups()) {
                for (cloud.hytora.driver.permission.Permission permission : group.getPermissions()) {
                    perms.put(permission.getPermission(), new PermissionAttachmentInfo(this, permission.getPermission(), null, true));
                }

                //denied permissions
                for (String permission : group.getDeniedPermissions()) {
                    perms.put(permission, new PermissionAttachmentInfo(this, permission, null, false));
                }

                for (String taskPermission : group.getTaskPermissions(CloudDriver.getInstance().thisSidesClusterParticipant(ICloudServer.class).getTask().getName())) {
                    perms.put(taskPermission, new PermissionAttachmentInfo(this, taskPermission, null, true));
                }

            }
        } catch (NullPointerException e) {
            CloudDriver.getInstance().getScheduler().scheduleDelayedTask(this::recalculatePermissions, 5L);
        }
    }


    public PermissionPlayer getPermissionPlayer() {
        if (permissionPlayer == null)
            permissionPlayer = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPlayerByUniqueIdOrNull(player.getUniqueId());

        return permissionPlayer;
    }
}
