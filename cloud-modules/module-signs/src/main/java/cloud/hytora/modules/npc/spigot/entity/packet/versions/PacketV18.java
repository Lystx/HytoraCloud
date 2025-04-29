package cloud.hytora.modules.npc.spigot.entity.packet.versions;

import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;

public class PacketV18 extends PacketV17 {

  @Override
  public int version() {
    return 18;
  }

  public void updateGlowPacket(SpigotNPC spigotNpc, Object packet) throws ReflectiveOperationException {
    Utils.setValue(packet, "m", CacheRegistry.ENUM_CHAT_FORMAT_FIND.load().invoke(null, spigotNpc.getMeta().getGlowName()));
  }
}
