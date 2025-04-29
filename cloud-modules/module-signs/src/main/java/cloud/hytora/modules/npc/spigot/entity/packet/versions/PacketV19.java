package cloud.hytora.modules.npc.spigot.entity.packet.versions;

import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import org.bukkit.Bukkit;

public class PacketV19 extends PacketV18 {
    public int version() {
        return 19;
    }


    public Object getPlayerPacket(Object nmsWorld, GameProfile gameProfile) throws ReflectiveOperationException {
        try {
            return CacheRegistry.PLAYER_CONSTRUCTOR_NEW_1.load().newInstance(CacheRegistry.GET_SERVER_METHOD
                    .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile, null);
        } catch (Throwable e) {
            return CacheRegistry.PLAYER_CONSTRUCTOR_NEW_2.load().newInstance(CacheRegistry.GET_SERVER_METHOD
                    .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile);
        }
    }
}
