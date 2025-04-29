package cloud.hytora.modules.npc.spigot.entity.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCSkin;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplySkinFetcherListener implements SkinFetcherListener {

    private final SpigotNPC spigotNpc;

    @Override
    public void onComplete(GameProfile gameProfile) {
        Property textureProperty = GameProfiles.getTextureProperty(gameProfile);
        if (textureProperty != null) {
            spigotNpc.changeSkin(NPCSkin.forValues(
                    textureProperty.getValue(), textureProperty.getSignature()));
        }
    }
}
