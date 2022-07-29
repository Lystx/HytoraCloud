package cloud.hytora.modules;

import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.impl.DefaultPermission;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class DefaultPermissionManager implements PermissionManager {

    @NotNull
    @Override
    public Permission createPermission(@NotNull String permission, @NotNull long expirationDate) {
        return new DefaultPermission(permission, expirationDate);
    }

    @Override
    public PermissionPlayer createPlayer(String name, UUID uniqueId) {
        return new DefaultPermissionPlayer(name, uniqueId);
    }

    @NotNull
    @Override
    public PermissionGroup createPermissionGroup(@NotNull String name) {
        return new DefaultPermissionGroup(name, "", "", "", "", "", 1, false, new ArrayList<>(), new HashMap<>());
    }
}
