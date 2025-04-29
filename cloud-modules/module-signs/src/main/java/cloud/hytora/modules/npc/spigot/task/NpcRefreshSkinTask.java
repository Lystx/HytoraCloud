package cloud.hytora.modules.npc.spigot.task;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.CloudNPC;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.skin.ApplySkinFetcherListener;
import cloud.hytora.modules.npc.spigot.entity.skin.SkinFetcher;

import java.util.HashSet;
import java.util.Set;

public class NpcRefreshSkinTask implements Runnable {
    private final Set<Integer> outgoingRefresh = new HashSet<>();
    private final SkinFetcher skinFetcher;

    private int count = 0;

    public NpcRefreshSkinTask(SkinFetcher skinFetcher) {
        this.skinFetcher = Preconditions.checkNotNull(skinFetcher);

    }

    @Override
    public void run() {
        NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

        for (CloudNPC cloudNPC : factory.getActiveNPCs()) {
            SpigotNPC spigotNpc = (SpigotNPC) cloudNPC;
            int refreshSkinDuration = spigotNpc.getMeta().getRefreshSkinDuration();
            if (refreshSkinDuration != 0 && count % refreshSkinDuration == 0) {
                int id = spigotNpc.getMeta().getId();
                if (outgoingRefresh.contains(id)) {
                    continue;
                }
                outgoingRefresh.add(id);
                skinFetcher.fetchGameProfile(
                        spigotNpc.getMeta().getSkinName(),
                        new ApplySkinFetcherListener(spigotNpc) {
                            @Override
                            public void onComplete(GameProfile gameProfile) {
                                outgoingRefresh.remove(id);
                                super.onComplete(gameProfile);
                            }

                            @Override
                            public void onError(Throwable error) {
                                outgoingRefresh.remove(id);
                                super.onError(error);
                            }
                        });
            }
        }
        count++;
    }
}
