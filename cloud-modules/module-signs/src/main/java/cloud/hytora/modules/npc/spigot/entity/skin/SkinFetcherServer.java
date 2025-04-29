package cloud.hytora.modules.npc.spigot.entity.skin;

import cloud.hytora.driver.http.HttpClient;
import com.mojang.authlib.GameProfile;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;

public abstract class SkinFetcherServer {

    private static final Duration SKIN_REQUEST_DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    protected abstract URI getUriForRequest(String skinName);

    public abstract GameProfile readProfile(
            String skinName, HttpClient.Response httpResponse);

    public HttpClient.Request prepareRequest(String skinName) {
        return new HttpClient.Request(
                getUriForRequest(skinName),
                SKIN_REQUEST_DEFAULT_TIMEOUT,
                new HashMap<>(),
                null
        );
    }
}
