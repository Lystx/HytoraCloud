package cloud.hytora.modules.dashboard.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.http.api.HttpAuthHandler;
import cloud.hytora.driver.common.http.api.HttpAuthUser;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PlayerAuthHandler implements HttpAuthHandler {

    @Nullable
    @Override
    public HttpAuthUser getAuthUser(@Nonnull String token) {
        String[] arguments = token.split(":");
        if (arguments.length != 2) return null;

        CloudOfflinePlayer player = CloudDriver.getInstance().getPlayerManager().getCachedOfflinePlayerOrRefresh(arguments[0]);
        if (player == null) return null;
        String playerToken = player.getProperties().getString("rest-api-token");
        if (playerToken == null) return null;
        if (!playerToken.equals(arguments[1])) return null;

        return new PlayerAuthUser(player.asPermissionPlayer());
    }
}
