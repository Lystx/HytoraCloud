package cloud.hytora.modules.npc.spigot.entity;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.CloudNPCMeta;
import cloud.hytora.modules.npc.api.NPCFunction;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCAction;
import cloud.hytora.modules.npc.spigot.entity.npc.types.ItemSlot;
import cloud.hytora.modules.npc.spigot.entity.npc.types.NPCType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Getter
@Accessors(chain = true)
@Setter
public class SpigotNPCMeta implements CloudNPCMeta {

    private int id;
    private int refreshSkinDuration, updateSkinAfterSpawn;
    private double hologramHeight;

    private String skin;
    private String signature;
    private String skinName = "Steve";
    private String pathName;
    private String glowName;
    private String internalName;
    private String displayName;

    private CloudEntityLocation<Double, Float> location;
    private NPCType type;

    private Collection<Hologram.LineData> hologramLines;
    private Collection<UUID> specificViewers;
    private Collection<NPCAction> clickActions;
    private Map<ItemSlot, ItemStack> npcEquip;
    private Map<String, Boolean> functions;
    private Map<String, String[]> customizationMap;

    public SpigotNPCMeta(int id) {
        this.id = id;
        this.skin = "";
        this.signature = "";
        this.type = NPCType.PLAYER;
        this.specificViewers = new ArrayList<>();
        this.clickActions = new ArrayList<>();
        this.npcEquip = new HashMap<>();
        this.customizationMap = new HashMap<>();
        this.functions = new HashMap<>();
    }

    public SpigotNPCMeta() {


        this(
                CloudDriver.getInstance()
                        
                        .getProvider(NPCManager.class)
                        .getNPCFactory(
                                CloudDriver.getInstance()
                                        .getServiceManager()
                                        .thisService()
                        )
                        .nextId()
        );
    }

    @Override
    public CloudNPCMeta updateSkinAfterSpawn(int seconds) {
        this.updateSkinAfterSpawn = seconds;
        return this;
    }

    public CloudNPCMeta setDisplayName(String displayName) {
        this.displayName = displayName;
        this.addFunction(NPCFunction.HOLO);
        this.addHologramLine(displayName);
        return this;
    }

    @Override
    public CloudNPCMeta setSpecificViewers(UUID... viewers) {
        this.specificViewers.addAll(Arrays.asList(viewers));
        return this;
    }

    public CloudNPCMeta setSkinName(String skinName) {
        this.skinName = skinName;
        this.setType(NPCType.PLAYER);
        return this;
    }

    @Override
    public CloudNPCMeta nextId() {
        return setId(CloudDriver.getInstance()
                
                .getProvider(NPCManager.class)
                .getNPCFactory(
                        CloudDriver.getInstance()
                                .getServiceManager()
                                .thisService()
                )
                .nextId());
    }

    @Override
    public CloudNPCMeta addHologramLine(String line) {
        if (this.hologramLines == null) {
            this.hologramLines = new ArrayList<>();
        }
        this.hologramLines.add(new Hologram.LineData(line));
        return this;
    }

    @Override
    public CloudNPCMeta addHologramLine(Supplier<String> line, TimeUnit unit, long value) {
        if (this.hologramLines == null) {
            this.hologramLines = new ArrayList<>();
        }
        this.hologramLines.add(new Hologram.LineData(line, unit, value));
        return this;
    }

    @Override
    public CloudNPCMeta addFunction(NPCFunction function) {
        this.functions.put(function.name().toString(), true);
        return this;
    }

    @Override
    public CloudNPCMeta addClickAction(NPCAction action) {
        this.clickActions.add(action);
        return this;
    }

    public Map<ItemSlot, ItemStack> getNpcEquip() {
        return this.npcEquip;
    }

    public void setNpcEquip(Map<ItemSlot, ItemStack> npcEquip) {
        this.npcEquip = npcEquip;
    }

    public Map<String, String[]> getCustomizationMap() {
        return this.customizationMap;
    }



}
