package cloud.hytora.modules.npc.spigot.entity.skin;

import cloud.hytora.common.task.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;

import javax.annotation.Nullable;

public interface SkinFetcher {

    Task<GameProfile> fetchGameProfile(String name, @Nullable SkinFetcherListener listener);
}
