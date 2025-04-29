package cloud.hytora.modules.npc.spigot.entity.packet.versions;

import com.mojang.authlib.GameProfile;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;
import org.bukkit.Bukkit;

public class PacketV17 extends PacketV16 {
  public int version() {
    return 17;
  }
  
  public Object getPlayerPacket(Object nmsWorld, GameProfile gameProfile) throws ReflectiveOperationException {
    return CacheRegistry.PLAYER_CONSTRUCTOR_NEW.load().newInstance(CacheRegistry.GET_SERVER_METHOD.load().invoke(Bukkit.getServer()), nmsWorld, gameProfile);
  }
  
  public void updateGlowPacket(SpigotNPC spigotNpc, Object packet) throws ReflectiveOperationException {
    Utils.setValue(packet, "n", CacheRegistry.ENUM_CHAT_FORMAT_FIND.load().invoke(null, spigotNpc.getMeta().getGlowName()));
  }
  
  public Object getClickType(Object interactPacket) {
    return "INTERACT";
  }
}
