package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.*;
import cloud.hytora.modules.global.impl.DefaultPermission;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class DefaultPermissionManager implements PermissionManager {

    public DefaultPermissionManager() {
        CloudDriver.getInstance().getProviderRegistry().setProvider(PermissionChecker.class, this);
    }

    @Override
    public boolean hasPermission(UUID playerUniqueId, String permission) {
        return getPlayerAsyncByUniqueId(playerUniqueId).mapOrElse(p -> p.hasPermission("*") || p.hasPermission(permission), () -> false);
    }

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
