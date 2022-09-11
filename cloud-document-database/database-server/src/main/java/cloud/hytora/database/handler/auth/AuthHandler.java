package cloud.hytora.database.handler.auth;

import cloud.hytora.database.DocumentDatabase;
import cloud.hytora.database.handler.auth.user.AllowedAuthUser;
import cloud.hytora.http.api.HttpAuthHandler;
import cloud.hytora.http.api.HttpAuthUser;
import cloud.hytora.http.api.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class AuthHandler implements HttpAuthHandler {

    @Nullable
    @Override
    public HttpAuthUser getAuthUser(@Nonnull HttpContext context, @NotNull String token) {
        if (token.equalsIgnoreCase(DocumentDatabase.getInstance().getConfig().getToken())) {
            return new AllowedAuthUser();
        }

        return null;
    }
}
