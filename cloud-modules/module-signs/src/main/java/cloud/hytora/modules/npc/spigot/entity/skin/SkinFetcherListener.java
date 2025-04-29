package cloud.hytora.modules.npc.spigot.entity.skin;

import com.mojang.authlib.GameProfile;

public interface SkinFetcherListener {

  default void onComplete(GameProfile gameProfile) {}

  default void onError(Throwable error) {}
}
