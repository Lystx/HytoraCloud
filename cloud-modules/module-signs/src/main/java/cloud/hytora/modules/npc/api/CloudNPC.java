package cloud.hytora.modules.npc.api;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;

import java.util.Collection;
import java.util.UUID;

public interface CloudNPC {

    UUID getUniqueId();

    CloudNPCMeta getMeta();

    CloudNPC setMeta(CloudNPCMeta meta);

    Collection<UUID> getViewerIds();

    void spawn();

    void despawn();

    void despawn(EntityPlayerConnection connection);

    void teleport(CloudEntityLocation<Double, Float> location);


}
