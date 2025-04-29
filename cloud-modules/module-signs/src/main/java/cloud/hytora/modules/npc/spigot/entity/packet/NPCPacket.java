package cloud.hytora.modules.npc.spigot.entity.packet;

import cloud.hytora.modules.npc.api.NPCFunction;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.npc.types.ItemSlot;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.npc.types.NPCType;
import cloud.hytora.modules.npc.spigot.entity.utility.ReflectionUtils;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public interface NPCPacket {
    int version();

    @PacketValue(keyName = "playerPacket")
    Object getPlayerPacket(Object paramObject, GameProfile paramGameProfile) throws ReflectiveOperationException;

    @PacketValue(keyName = "spawnPacket")
    Object getSpawnPacket(Object paramObject, boolean paramBoolean) throws ReflectiveOperationException;

    Object convertItemStack(int paramInt, ItemSlot paramItemSlot, ItemStack paramItemStack) throws ReflectiveOperationException;

    Object getClickType(Object paramObject) throws ReflectiveOperationException;

    Object getMetadataPacket(int paramInt, Object paramObject) throws ReflectiveOperationException;

    @PacketValue(keyName = "hologramSpawnPacket", valueType = ValueType.ARGUMENTS)
    Object getHologramSpawnPacket(Object paramObject) throws ReflectiveOperationException;

    @PacketValue(keyName = "destroyPacket", valueType = ValueType.ARGUMENTS)
    default Object getDestroyPacket(int entityId) throws ReflectiveOperationException {
        return CacheRegistry.PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR.load().newInstance(
                CacheRegistry.PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR.load().getParameterTypes()[0].isArray() ? new int[]{entityId} : entityId);
    }

    @PacketValue(keyName = "enumSlot", valueType = ValueType.ARGUMENTS)
    default Object getItemSlot(int slot) {
        return CacheRegistry.ENUM_ITEM_SLOT.getEnumConstants()[slot];
    }

    @PacketValue(keyName = "removeTab")
    default Object getTabRemovePacket(Object nmsEntity) throws ReflectiveOperationException {
        try {
            return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.REMOVE_PLAYER_FIELD
                            .load(),
                    Collections.singletonList(nmsEntity));
        } catch (Throwable throwable) {
            boolean useOldMethod = CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_REMOVE_CLASS != null;
            if (useOldMethod) {
                return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_REMOVE_CONSTRUCTOR.load()
                        .newInstance(Collections.singletonList(CacheRegistry.GET_UNIQUE_ID_METHOD.load().invoke(nmsEntity)));
            } else {
                return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.REMOVE_PLAYER_FIELD
                                .load(),
                        nmsEntity);
            }
        }
    }

    @PacketValue(keyName = "equipPackets")
    ImmutableList<Object> getEquipPackets(SpigotNPC paramSpigotNPC) throws ReflectiveOperationException;

    @PacketValue(keyName = "scoreboardPackets")
    default ImmutableList<Object> updateScoreboard(SpigotNPC spigotNpc) throws ReflectiveOperationException {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        boolean isVersion17 = (Utils.BUKKIT_VERSION > 16);
        boolean isVersion9 = (Utils.BUKKIT_VERSION > 8);
        Object scoreboardTeamPacket = isVersion17 ? CacheRegistry.SCOREBOARD_TEAM_CONSTRUCTOR.load().newInstance(null, spigotNpc.getGameProfile().getName()) : CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.load().newInstance();
        if (!isVersion17) {
            Utils.setValue(scoreboardTeamPacket, "a", spigotNpc.getGameProfile().getName());
            Utils.setValue(scoreboardTeamPacket, isVersion9 ? "i" : "h", 1);
        }
        builder.add(isVersion17 ? CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CREATE_V1.load().invoke(null, scoreboardTeamPacket) : scoreboardTeamPacket);
        if (isVersion17) {
            scoreboardTeamPacket = CacheRegistry.SCOREBOARD_TEAM_CONSTRUCTOR.load().newInstance(null, spigotNpc.getGameProfile().getName());
            if (Utils.BUKKIT_VERSION > 17) {
                Utils.setValue(scoreboardTeamPacket, "d", spigotNpc.getGameProfile().getName());
                ReflectionUtils.findFieldForClassAndSet(scoreboardTeamPacket, CacheRegistry.ENUM_TAG_VISIBILITY, CacheRegistry.ENUM_TAG_VISIBILITY_NEVER_FIELD.load());
                Utils.setValue(scoreboardTeamPacket, "m", CacheRegistry.ENUM_CHAT_FORMAT_FIND.load().invoke(null, "DARK_GRAY"));
            } else {
                Utils.setValue(scoreboardTeamPacket, "e", spigotNpc.getGameProfile().getName());
                Utils.setValue(scoreboardTeamPacket, "l", CacheRegistry.ENUM_TAG_VISIBILITY_NEVER_FIELD.load());
            }
        } else {
            scoreboardTeamPacket = CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.load().newInstance();
            Utils.setValue(scoreboardTeamPacket, "a", spigotNpc.getGameProfile().getName());
            Utils.setValue(scoreboardTeamPacket, "e", "never");
            Utils.setValue(scoreboardTeamPacket, isVersion9 ? "i" : "h", 0);
        }
        Collection<String> collection = (Collection<String>) (isVersion17 ?
                CacheRegistry.SCOREBOARD_PLAYER_LIST.load().invoke(scoreboardTeamPacket) : Utils.getValue(scoreboardTeamPacket, isVersion9 ? "h" : "g"));
        if (spigotNpc.getMeta().getType() == NPCType.PLAYER) {
            collection.add(spigotNpc.getGameProfile().getName());
        } else {
            collection.add(spigotNpc.getUniqueId().toString());
        }
        if (allowGlowColor() && spigotNpc.getMeta().isFunction(NPCFunction.GLOW))
            updateGlowPacket(spigotNpc, scoreboardTeamPacket);
        builder.add(isVersion17 ? CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CREATE.load().invoke(null, scoreboardTeamPacket, Boolean.TRUE) : scoreboardTeamPacket);
        return builder.build();
    }

    void updateGlowPacket(SpigotNPC paramSpigotNPC, Object paramObject) throws ReflectiveOperationException;

    boolean allowGlowColor();

    default void update(PacketCache packetCache) throws ReflectiveOperationException {
        packetCache.flushCache("scoreboardPackets");
    }
}
