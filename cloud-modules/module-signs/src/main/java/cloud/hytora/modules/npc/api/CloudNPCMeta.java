package cloud.hytora.modules.npc.api;

import cloud.hytora.common.location.ImmutableLocation;
import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.common.location.impl.DefaultLocation;
import cloud.hytora.modules.npc.spigot.entity.Hologram;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCAction;
import cloud.hytora.modules.npc.spigot.entity.npc.types.NPCType;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface CloudNPCMeta {


    NPCType getType();

    CloudNPCMeta setType(NPCType type);

    int getId();

    CloudNPCMeta setId(int id);

    CloudNPCMeta nextId();

    String getInternalName();

    CloudNPCMeta setInternalName(String internalName);

    String getDisplayName();

    CloudNPCMeta setDisplayName(String displayName);

    Collection<UUID> getSpecificViewers();

    CloudNPCMeta setSpecificViewers(UUID... viewers);


    double getHologramHeight();

    CloudNPCMeta setHologramHeight(double hologramHeight);

    @Deprecated
    String getSkin();

    @Deprecated
    CloudNPCMeta setSkin(String skin);

    String getSkinName();

    CloudNPCMeta setSkinName(String skin);

    CloudNPCMeta updateSkinAfterSpawn(int seconds);

    String getSignature();

    CloudNPCMeta setSignature(String signature);

    String getPathName();

    CloudNPCMeta setPathName(String pathName);

    String getGlowName();

    CloudNPCMeta setGlowName(String glowName);

    Collection<Hologram.LineData> getHologramLines();

    CloudNPCMeta addHologramLine(String line);

    CloudNPCMeta addHologramLine(Supplier<String> line, TimeUnit unit, long value);

    CloudNPCMeta setHologramLines(Collection<Hologram.LineData> hologramLines);

    Map<String, Boolean> getFunctions();

    CloudNPCMeta addFunction(NPCFunction function);

    default boolean isFunction(NPCFunction function) {
        return getFunctions().keySet().stream().anyMatch(s -> s.equalsIgnoreCase(function.name()));
    }

    int getRefreshSkinDuration();

    CloudNPCMeta setRefreshSkinDuration(int refreshSkinDuration);

    Collection<NPCAction> getClickActions();

    CloudNPCMeta addClickAction(NPCAction action);

    CloudEntityLocation<Double, Float> getLocation();

    CloudNPCMeta setLocation(CloudEntityLocation<Double, Float> location);

}
