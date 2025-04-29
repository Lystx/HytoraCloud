package cloud.hytora.modules.npc.spigot.entity.user;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class EntityPlayerConnection {

    public static final Map<UUID, EntityPlayerConnection> CONNECTIONS = new HashMap<>();
    public static final Map<UUID, Collection<Integer>> ENTITY_TYPE = new HashMap<>();

    private final UUID uniqueId;
    private GameProfile gameProfile;
    private Object playerConnection;

    public EntityPlayerConnection(UUID uuid) {
        this.uniqueId = uuid;
    }

    public void inject() {

        try {
            Object playerHandle = CacheRegistry.GET_HANDLE_PLAYER_METHOD.load().invoke(toPlayer());
            this.gameProfile = (GameProfile) CacheRegistry.GET_PROFILE_METHOD.load().invoke(playerHandle, new Object[0]);
            Channel channel = (Channel) CacheRegistry.CHANNEL_FIELD.load()
                    .get(CacheRegistry.NETWORK_MANAGER_FIELD.load()
                    .get(this.playerConnection = CacheRegistry.PLAYER_CONNECTION_FIELD.load().get(playerHandle)));
            if (channel.pipeline().names().contains("npc_interact")) {
                channel.pipeline().remove("npc_interact");
            }
            channel.pipeline().addAfter("decoder", "npc_interact", new UserEntityInteractHandler(this));
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new IllegalStateException("can't create player " + uniqueId.toString(), e.getCause());
        }
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(this.uniqueId);
    }


    public static EntityPlayerConnection find(Player player) {
        return CONNECTIONS.computeIfAbsent(player.getUniqueId(), EntityPlayerConnection::new);
    }

    public static void typeChangedFor(int entiyId, UUID uniqueId) {
        Collection<Integer> integers = ENTITY_TYPE.get(uniqueId);
        if (integers == null) {
            integers = new ArrayList<>();
        }
        integers.add(entiyId);
        ENTITY_TYPE.put(uniqueId, integers);
    }

    public static boolean hasTypeChangedFor(int entityId, UUID uniqueId) {
        Collection<Integer> integers = ENTITY_TYPE.get(uniqueId);
        if (integers == null) {
            integers = new ArrayList<>();
        }
        return integers.stream().anyMatch(i -> i == entityId);
    }
}
