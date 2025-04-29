package cloud.hytora.modules.npc.spigot.entity.packet.versions;

import cloud.hytora.modules.npc.spigot.entity.packet.NPCPacket;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.npc.types.ItemSlot;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.Map;

public class PacketV8 implements NPCPacket {
    public int version() {
        return 8;
    }

    public Object getPlayerPacket(Object nmsWorld, GameProfile gameProfile) throws ReflectiveOperationException {
        Constructor<?> constructor = (Utils.BUKKIT_VERSION > 13) ? CacheRegistry.PLAYER_INTERACT_MANAGER_NEW_CONSTRUCTOR.load() : CacheRegistry.PLAYER_INTERACT_MANAGER_OLD_CONSTRUCTOR.load();
        return CacheRegistry.PLAYER_CONSTRUCTOR_OLD.load().newInstance(CacheRegistry.GET_SERVER_METHOD
                .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile, constructor

                .newInstance(nmsWorld));
    }

    public Object getSpawnPacket(Object nmsEntity, boolean isPlayer) throws ReflectiveOperationException {
        return isPlayer ? CacheRegistry.PACKET_PLAY_OUT_NAMED_ENTITY_CONSTRUCTOR.load().newInstance(nmsEntity)
                : CacheRegistry.PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR.load().newInstance(nmsEntity);
    }

    public Object convertItemStack(int entityId, ItemSlot itemSlot, ItemStack itemStack) throws ReflectiveOperationException {
        return CacheRegistry.PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR_OLD.load().newInstance(entityId,
                itemSlot.getSlotOld(), CacheRegistry.AS_NMS_COPY_METHOD
                        .load().invoke(CacheRegistry.CRAFT_ITEM_STACK_CLASS, itemStack));
    }

    public Object getClickType(Object interactPacket) throws ReflectiveOperationException {
        return Utils.getValue(interactPacket, "action");
    }

    public Object getMetadataPacket(int entityId, Object nmsEntity) throws ReflectiveOperationException {
        Object dataWatcher = CacheRegistry.GET_DATA_WATCHER_METHOD.load().invoke(nmsEntity);
        try {
            return CacheRegistry.PACKET_PLAY_OUT_ENTITY_META_DATA_CONSTRUCTOR.load().newInstance(
                    entityId, dataWatcher, true);
        } catch (Exception e2) {
            return CacheRegistry.PACKET_PLAY_OUT_ENTITY_META_DATA_CONSTRUCTOR_V1
                    .load()
                    .newInstance(entityId,
                            CacheRegistry.GET_DATAWATCHER_B_LIST.load().invoke(dataWatcher));
        }
    }

    public Object getHologramSpawnPacket(Object armorStand) throws ReflectiveOperationException {
        return CacheRegistry.PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR.load().newInstance(armorStand);
    }

    public ImmutableList<Object> getEquipPackets(SpigotNPC spigotNpc) throws ReflectiveOperationException {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (Map.Entry<ItemSlot, ItemStack> stackEntry : spigotNpc.getMeta().getNpcEquip().entrySet()) {
            builder.add(CacheRegistry.PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR_OLD.load().newInstance(spigotNpc.getEntityID(),
                    stackEntry.getKey().getSlotOld(),
                    convertItemStack(spigotNpc.getEntityID(), stackEntry.getKey(), stackEntry.getValue())));
        }
        return builder.build();
    }

    public void updateGlowPacket(SpigotNPC spigotNpc, Object packet) throws ReflectiveOperationException {
        throw new IllegalStateException("Glow color is not supported for 1.8 version.");
    }

    public boolean allowGlowColor() {
        return false;
    }
}
