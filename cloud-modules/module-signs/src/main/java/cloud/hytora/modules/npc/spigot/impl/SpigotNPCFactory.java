package cloud.hytora.modules.npc.spigot.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.npc.api.CloudNPC;
import cloud.hytora.modules.npc.api.CloudNPCMeta;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPCMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpigotNPCFactory implements NPCFactory {

    private final String service;
    private final Map<Integer, CloudNPC> activeNPCs;

    public SpigotNPCFactory(ICloudService service) {
        this.service = service.getName();
        this.activeNPCs = new HashMap<>();
    }

    @Override
    public int nextId() {
        return activeNPCs.size() + 1;
    }

    @Override
    public ICloudService getService() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(service);
    }

    @Override
    public CloudNPC createNPC(CloudNPCMeta model) {
        return new SpigotNPC().setMeta(model);
    }

    @Override
    public CloudNPCMeta createMeta() {
        return new SpigotNPCMeta();
    }

    @Override
    public Collection<CloudNPC> getActiveNPCs() {
        return activeNPCs.values();
    }

    @Override
    public void unregister(CloudNPC npc) {
        activeNPCs.remove(npc.getMeta().getId());
    }

    @Override
    public void updateCache() {

        NPCManager npcManager = CloudDriver.getInstance().getProvider(NPCManager.class);

        npcManager.update(this);
    }

    @Override
    public void register(CloudNPC npc) {
        activeNPCs.put(npc.getMeta().getId(), npc);
    }

    @Override
    public CloudNPC getNPC(int id) {
        return activeNPCs.get(id);
    }

    @Override
    public CloudNPC getNPC(String name) {

        return activeNPCs.values().stream().filter(npc -> npc.getMeta().getInternalName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
