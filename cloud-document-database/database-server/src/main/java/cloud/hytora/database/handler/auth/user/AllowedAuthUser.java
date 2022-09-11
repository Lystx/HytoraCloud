package cloud.hytora.database.handler.auth.user;

import cloud.hytora.http.api.HttpAuthUser;
import org.jetbrains.annotations.NotNull;

public class AllowedAuthUser implements HttpAuthUser {

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }
}
