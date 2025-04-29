package cloud.hytora.modules.npc.spigot.entity.skin;

import cloud.hytora.document.Document;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import java.net.URI;
import java.util.UUID;

public class AshconSkinFetcherServer extends AbstractSkinFetcherServer {

    @Override
    protected URI getUriForRequest(String skinName) {
        return URI.create(String.format("https://api.ashcon.app/mojang/v2/user/%s", skinName));
    }

    @Override
    protected GameProfile readProfile(String skinName, Document document) {

        UUID uuid = UUID.fromString(document.get("uuid").toString());
        String username = document.get("username").toString();

        Document textures = document.get("textures").toDocument();
        Document skin = textures.get("raw").toDocument();

        return GameProfiles.newGameProfile(uuid, username,
                skin.get("value").toString(), skin.get("signature").toString());
    }
}
