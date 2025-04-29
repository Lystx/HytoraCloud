package cloud.hytora.modules.npc.api;

import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.npc.spigot.entity.skin.SkinFetcher;

public interface NPCManager {

    SkinFetcher getSkinFetcher();

    NPCFactory getNPCFactory(ICloudService cloudService);

    void update(NPCFactory factory);

    void load(Object instance);


    void unload();
}
