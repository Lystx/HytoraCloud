package cloud.hytora.modules.npc.spigot.entity.packet;

import cloud.hytora.modules.npc.spigot.entity.packet.versions.*;
import com.google.common.collect.ImmutableSet;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;

import java.util.Comparator;

public class PacketFactory {

    public static final ImmutableSet<NPCPacket> ALL = ImmutableSet.of(new PacketV8(), new PacketV9(),
            new PacketV16(), new PacketV17(), new PacketV18(), new PacketV19());

    public static final NPCPacket PACKET_FOR_CURRENT_VERSION = findPacketForVersion(Utils.BUKKIT_VERSION);

    public static NPCPacket findPacketForVersion(int version) {
        return ALL.stream()
                .filter(packet -> (version >= packet.version()))
                .max(Comparator.comparing(NPCPacket::version))
                .orElseThrow(() -> new IllegalArgumentException("No packet instance found for version: " + version));
    }
}
