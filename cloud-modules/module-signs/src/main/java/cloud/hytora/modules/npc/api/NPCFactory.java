package cloud.hytora.modules.npc.api;

import cloud.hytora.driver.services.ICloudService;

import java.util.Collection;

public interface NPCFactory {

    int nextId();

    ICloudService getService();

    CloudNPC createNPC(CloudNPCMeta model);

    CloudNPCMeta createMeta();

    Collection<CloudNPC> getActiveNPCs();

    void unregister(CloudNPC npc);

    void register(CloudNPC npc);

    void updateCache();

    CloudNPC getNPC(int id);

    CloudNPC getNPC(String name);



}
