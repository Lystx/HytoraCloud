package cloud.hytora.modules.npc.spigot.entity;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.*;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCSkin;
import cloud.hytora.modules.npc.spigot.entity.npc.types.NPCType;
import cloud.hytora.modules.npc.spigot.entity.skin.ApplySkinFetcherListener;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.packet.PacketCache;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;
import cloud.hytora.modules.npc.spigot.entity.utility.location.SpigotLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class SpigotNPC implements CloudNPC {

    private final Set<EntityPlayerConnection> viewers = new HashSet<>();
    private final PacketCache packets = new PacketCache();

    @Setter
    private SpigotNPCMeta meta;
    private final Hologram hologram;
    private String internalName;
    private NPCSkin npcSkin;

    private long lastMove = -1L;
    private int entityID;

    private Object glowColor;
    private Object tabConstructor, updateTabConstructor;
    private Object nmsEntity;
    private Object bukkitEntity;

    private UUID uniqueId;
    private GameProfile gameProfile;

    public SpigotNPC(SpigotNPCMeta meta) {
        this();
        this.meta = meta;
    }

    public SpigotNPC() {
        this.hologram = new Hologram(this);
        this.internalName = Utils.randomString(6);
        this.gameProfile = new GameProfile(UUID.randomUUID(), this.internalName);
    }

    @Override
    public void spawn() throws IllegalStateException {
        NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

        if (factory.getNPC(getMeta().getId()) != null) {
            throw new IllegalStateException("npc with id " + getMeta().getId() + " already exists.");
        }

        this.gameProfile.getProperties().put("textures", new Property("textures", this.meta.getSkin(), this.meta.getSignature()));
        updateProfile(this.gameProfile.getProperties());
        setLocation(((SpigotLocation) getMeta().getLocation()).bukkitLocation(), false);
        this.hologram.createHologram();
        this.meta.getCustomizationMap().forEach((key, value) -> this.meta.getType().updateCustomization(this, key, value));

        factory.register(this);
        factory.updateCache();


    }

    @Override
    public void despawn() {

        NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

        factory.unregister(this);
        factory.updateCache();
        this.deleteViewers();

    }

    @Override
    public CloudNPC setMeta(CloudNPCMeta meta) {
        this.meta = (SpigotNPCMeta) meta;

        if (meta.getInternalName() != null) {
            this.internalName = meta.getInternalName();
            this.gameProfile = new GameProfile(UUID.randomUUID(), this.internalName);
        }
        if (meta.getSkin() != null && meta.getSignature() != null) {
            this.npcSkin = NPCSkin.forValues(meta.getSkin(), meta.getSignature());
        }

        if (meta.getType() != null) {
            changeType(meta.getType());
        }

        if (meta.getSkinName() != null) {
            fetchSkin();
        }

        SpigotNPCMeta npcMeta = (SpigotNPCMeta) meta;
        int updateSkinAfterSpawn = npcMeta.getUpdateSkinAfterSpawn();
        if (updateSkinAfterSpawn != 0) {
            CloudDriver.getInstance()
                    .getScheduler()
                    .scheduleDelayedTaskAsync(this::fetchSkin, TimeUnit.SECONDS.toMillis(updateSkinAfterSpawn));
        }

        return this;
    }

    private void fetchSkin() {
        CloudDriver
                .getInstance()
                
                .getProvider(NPCManager.class)
                .getSkinFetcher()
                .fetchGameProfile(
                        meta.getSkinName(),
                        new ApplySkinFetcherListener(this)
                );
    }

    @Override
    public void teleport(CloudEntityLocation<Double, Float> location) {
        this.setLocation(new Location(
                        Bukkit.getWorld(location.getWorld()),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch()
                ), true
        );
    }

    public void setLocation(Location location, boolean updateTime) {
        try {
            lookAt(null, location, true);
            if (updateTime)
                this.lastMove = System.nanoTime();
            this.meta.setLocation(new SpigotLocation(location = new Location(location.getWorld(), location.getBlockX() + 0.5D, location.getY(), location.getBlockZ() + 0.5D, location.getYaw(), location.getPitch())));

            CacheRegistry.SET_LOCATION_METHOD.load().invoke(this.nmsEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            Object npcTeleportPacket = CacheRegistry.PACKET_PLAY_OUT_ENTITY_TELEPORT_CONSTRUCTOR.load().newInstance(this.nmsEntity);
            this.viewers.forEach(player -> Utils.sendPackets(player, npcTeleportPacket));
            this.hologram.setLocation(location, this.meta.getType().getHoloHeight());
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }

    public void changeSkin(NPCSkin skinFetch) {
        this.meta.setSkin(skinFetch.getTexture());
        this.meta.setSignature(skinFetch.getSignature());
        this.gameProfile.getProperties().clear();
        this.gameProfile.getProperties().put("textures", new Property("textures",
                this.meta.getSkin(), this.meta.getSignature()));
        updateProfile(this.gameProfile.getProperties());
        deleteViewers();
    }

    public synchronized void changeType(NPCType npcType) {
        deleteViewers();
        try {
            Object nmsWorld = CacheRegistry.GET_HANDLE_WORLD_METHOD.load().invoke(getLocation().getWorld());
            boolean isPlayer = (npcType == NPCType.PLAYER);
            this.nmsEntity = isPlayer ? this.packets.getProxyInstance().getPlayerPacket(nmsWorld, this.gameProfile) : (Utils.versionNewer(14) ? npcType.getConstructor().newInstance(npcType.getNmsEntityType(), nmsWorld) : npcType.getConstructor().newInstance(nmsWorld));
            this.bukkitEntity = CacheRegistry.GET_BUKKIT_ENTITY_METHOD.load().invoke(this.nmsEntity);
            this.uniqueId = (UUID) CacheRegistry.GET_UNIQUE_ID_METHOD.load().invoke(this.nmsEntity);
            if (isPlayer) {
                try {
                    this.tabConstructor = CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.ADD_PLAYER_FIELD.load(), Collections.singletonList(this.nmsEntity));
                } catch (Throwable e) {
                    this.tabConstructor = CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.ADD_PLAYER_FIELD.load(), nmsEntity);
                    this.updateTabConstructor = CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.UPDATE_LISTED_FIELD.load(), nmsEntity);
                }
                //2nd layer skin

                try {
                    Object dataWatcherObject = CacheRegistry.GET_DATA_WATCHER_METHOD.load().invoke(nmsEntity);
                    if (Utils.versionNewer(9)) {
                        CacheRegistry.SET_DATA_WATCHER_METHOD.load().invoke(dataWatcherObject,
                                CacheRegistry.DATA_WATCHER_OBJECT_CONSTRUCTOR.load().newInstance(npcSkin.getLayerIndex(),
                                        CacheRegistry.DATA_WATCHER_REGISTER_FIELD.load()), (byte) 127);
                    } else CacheRegistry.WATCH_DATA_WATCHER_METHOD.load().invoke(dataWatcherObject, 10, (byte) 127);
                } catch (ReflectiveOperationException operationException) {
                    throw new RuntimeException(operationException);
                }
            }
            this.meta.setType(npcType);
            setLocation(getLocation(), false);
            this.packets.flushCache("spawnPacket", "removeTab");
            this.entityID = (Integer) CacheRegistry.GET_ENTITY_ID.load().invoke(this.nmsEntity);
            getPackets().getProxyInstance().update(this.packets);
            hologram.createHologram();
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }




    public synchronized void spawn(EntityPlayerConnection user) {
        if (this.viewers.contains(user)) {
            throw new IllegalStateException(user.getUniqueId().toString() + " is already a viewer.");
        }
        try {
            this.viewers.add(user);
            boolean npcIsPlayer = (this.meta.getType() == NPCType.PLAYER);
            if (meta.isFunction(NPCFunction.GLOW) || npcIsPlayer) {
                ImmutableList<Object> scoreboardPackets = this.packets.getProxyInstance().updateScoreboard(this);
                scoreboardPackets.forEach(p -> Utils.sendPackets(user, p));
            }
            if (!EntityPlayerConnection.hasTypeChangedFor(this.entityID, user.getUniqueId())) {
                changeType(meta.getType());
                EntityPlayerConnection.typeChangedFor(this.entityID, user.getUniqueId());
            }
            if (npcIsPlayer) {
                if (meta.isFunction(NPCFunction.MIRROR)) {
                    updateProfile(user.getGameProfile().getProperties());
                }
                Utils.sendPackets(user, this.tabConstructor, updateTabConstructor);
            }
            Utils.sendPackets(user, this.packets.getProxyInstance().getSpawnPacket(this.nmsEntity, npcIsPlayer));
            if (meta.isFunction(NPCFunction.HOLO)) {
                this.hologram.spawn(user);
            }
            updateMetadata(Collections.singleton(user));

            //equipment

            if (this.meta.getNpcEquip().isEmpty())
                return;
            try {
                ImmutableList<Object> equipPackets = this.packets.getProxyInstance().getEquipPackets(this);
                equipPackets.forEach(o -> Utils.sendPackets(user, o));
            } catch (ReflectiveOperationException operationException) {
                throw new RuntimeException(operationException.getCause());
            }

            lookAt(user, getLocation(), true);
            if (npcIsPlayer) {
                Object removeTabPacket = this.packets.getProxyInstance().getTabRemovePacket(this.nmsEntity);
                CloudDriver.getInstance().getScheduler().scheduleDelayedTaskAsync(() -> Utils.sendPackets(user,
                        removeTabPacket, updateTabConstructor), 60);
            }
        } catch (ReflectiveOperationException operationException) {
            despawn(user);
            throw new RuntimeException(operationException);
        }
    }

    @Override
    public Collection<UUID> getViewerIds() {
        return viewers.stream().map(EntityPlayerConnection::getUniqueId).collect(Collectors.toList());
    }

    public void despawn(EntityPlayerConnection user) {
        if (!this.viewers.contains(user)) {
            throw new IllegalStateException(user.getUniqueId().toString() + " is not a viewer.");
        }
        this.viewers.remove(user);
        handleDelete(user);
    }

    private void handleDelete(EntityPlayerConnection user) {
        try {
            if (this.meta.getType() == NPCType.PLAYER) {
                this.packets.getProxyInstance().getTabRemovePacket(this.nmsEntity);
            }
            this.hologram.delete(user);
            Utils.sendPackets(user, this.packets.getProxyInstance().getDestroyPacket(this.entityID));
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }

    public void lookAt(EntityPlayerConnection player, Location location, boolean rotation) {
        long lastMoveNanos = System.nanoTime() - this.lastMove;
        if (this.lastMove > 1L && lastMoveNanos < 1000000000L) {
            return;
        }
        Location direction = rotation ? location : ((SpigotLocation) this.meta.getLocation()).bukkitLocation().clone().setDirection(location.clone().subtract(((SpigotLocation) this.meta.getLocation()).bukkitLocation().clone()).toVector());
        try {
            Object lookPacket = CacheRegistry.PACKET_PLAY_OUT_ENTITY_LOOK_CONSTRUCTOR.load().newInstance(this.entityID, (byte) (int) (direction.getYaw() * 256.0F / 360.0F), (byte) (int) (direction.getPitch() * 256.0F / 360.0F), Boolean.TRUE);
            Object headRotationPacket = CacheRegistry.PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_CONSTRUCTOR.load().newInstance(this.nmsEntity, (byte) (int) (direction.getYaw() * 256.0F / 360.0F));
            if (player != null) {
                Utils.sendPackets(player, lookPacket, headRotationPacket);
            } else {
                this.viewers.forEach(players -> Utils.sendPackets(players, headRotationPacket));
            }
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }

    public void deleteViewers() {
        for (EntityPlayerConnection user : this.viewers) {
            handleDelete(user);
        }
        this.viewers.clear();
    }

    public void updateMetadata(Iterable<EntityPlayerConnection> users) {
        try {
            Object metaData = this.packets.getProxyInstance().getMetadataPacket(this.entityID, this.nmsEntity);
            for (EntityPlayerConnection user : users) {
                Utils.sendPackets(user, metaData);
            }
        } catch (ReflectiveOperationException operationException) {
            operationException.getCause().printStackTrace();

            operationException.printStackTrace();
        }
    }

    public void updateProfile(PropertyMap propertyMap) {
        if (this.meta.getType() != NPCType.PLAYER)
            return;
        try {
            Object gameProfileObj = CacheRegistry.GET_PROFILE_METHOD.load().invoke(this.nmsEntity);
            Utils.setValue(gameProfileObj, "name", this.gameProfile.getName());
            Utils.setValue(gameProfileObj, "id", this.gameProfile.getId());
            Utils.setValue(gameProfileObj, "properties", propertyMap);
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }


    public Location getLocation() {
        return (new SpigotLocation(this.meta.getLocation())).bukkitLocation();
    }


}
