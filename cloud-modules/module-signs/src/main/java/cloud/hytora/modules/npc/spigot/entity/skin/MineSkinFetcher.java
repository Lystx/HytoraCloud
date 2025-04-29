package cloud.hytora.modules.npc.spigot.entity.skin;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.http.HttpClient;
import cloud.hytora.driver.http.api.HttpMessage;
import cloud.hytora.driver.http.impl.NettyHttpRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

public class MineSkinFetcher extends AbstractSkinFetcherServer {
    private static final String FORM_UTF_MIME = "application/x-www-form-urlencoded; charset=utf-8";

    @Override
    protected URI getUriForRequest(String skinName) {
        try {
            return URI.create("https://api.mineskin.org/generate/url=" + URLEncoder.encode(skinName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return URI.create("https://api.mineskin.org/generate/url");
        }
    }

    @Override
    public HttpClient.Request prepareRequest(String skinName) {

        try {
            return super.prepareRequest(skinName)
                    .property(CONTENT_TYPE, FORM_UTF_MIME)
                    .setPost("=" + URLEncoder.encode(skinName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return super.prepareRequest(skinName);
        }
    }

    @Override
    protected GameProfile readProfile(String skinName, Document document) {
      Document data = document.getDocument("data");

        UUID uuid = UUID.fromString(data.getString("uuid"));
      Document skin = data.get("texture").toDocument();

        return GameProfiles.newGameProfile(uuid, skinName,
                skin.get("value").toString(), skin.get("signature").toString());
    }
}
