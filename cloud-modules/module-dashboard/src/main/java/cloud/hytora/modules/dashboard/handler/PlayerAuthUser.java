package cloud.hytora.modules.dashboard.handler;

import cloud.hytora.driver.common.http.api.HttpAuthUser;
import cloud.hytora.driver.module.permission.PermissionPlayer;

import javax.annotation.Nonnull;

public class PlayerAuthUser implements HttpAuthUser {

    private final PermissionPlayer player;

    public PlayerAuthUser(@Nonnull PermissionPlayer player) {
        this.player = player;
    }

    @Override
    public boolean hasPermission(@Nonnull String permission) {
        return player.hasPermission(permission);
    }
}