package cloud.hytora.driver.player.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.PlayerUnsafe;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DefaultPlayerUnsafe implements PlayerUnsafe {

    private final CloudOfflinePlayer offlinePlayer;

    @Override
    public PermissionGroup getHighestPermissionGroup() {
        String permsHighestGroup = offlinePlayer.getProperties().getString("module_perms_highest_group");
        return CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionGroupByNameOrNull(permsHighestGroup);
    }
}
