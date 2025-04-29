package cloud.hytora.modules.npc.spigot.entity.skin;

import cloud.hytora.document.Document;
import cloud.hytora.driver.http.HttpClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;


public abstract class AbstractSkinFetcherServer extends SkinFetcherServer {

    @Override
    public GameProfile readProfile(String skinName, HttpClient.Response httpResponse) {
        Document document = Document.newJsonDocument(httpResponse.getBody());
        return readProfile(skinName, document);
    }

    protected abstract GameProfile readProfile(String skinName, Document document);
}
