package cloud.hytora.modules.npc.spigot.entity.packet.versions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.npc.types.ItemSlot;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import javafx.util.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PacketV16 extends PacketV9 {
  public int version() {
    return 16;
  }
  
  public ImmutableList<Object> getEquipPackets(SpigotNPC spigotNpc) throws ReflectiveOperationException {
    List<Pair<?, ?>> pairs = Lists.newArrayListWithCapacity((ItemSlot.values()).length);
    for (Map.Entry<ItemSlot, ItemStack> entry : spigotNpc.getMeta().getNpcEquip().entrySet())
      pairs.add(new Pair<>(getItemSlot(entry
              .getKey().getSlot()),
            convertItemStack(spigotNpc.getEntityID(), entry.getKey(), entry.getValue())));
    return ImmutableList.of(CacheRegistry.PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR_V1.load().newInstance(spigotNpc.getEntityID(), pairs));
  }
}
