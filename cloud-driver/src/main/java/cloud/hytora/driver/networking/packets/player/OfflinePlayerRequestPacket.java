package cloud.hytora.driver.networking.packets.player;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

import static cloud.hytora.driver.networking.packets.player.OfflinePlayerRequestPacket.PayLoad.*;


public class OfflinePlayerRequestPacket extends Packet {

    public OfflinePlayerRequestPacket() {
        super(buf -> buf.writeEnum(GET_ALL));
    }

    public OfflinePlayerRequestPacket(CloudOfflinePlayer savedPlayer) {
        super(buf -> buf.writeEnum(SAVE_PLAYER).writeObject(savedPlayer));
    }

    public OfflinePlayerRequestPacket(String name) {
        super(buf -> buf.writeEnum(GET_BY_NAME).writeString(name));
    }

    public OfflinePlayerRequestPacket(UUID uniqueID) {
        super(buf -> buf.writeEnum(GET_BY_NAME).writeUniqueId(uniqueID));
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
    }

    public enum PayLoad {

        SAVE_PLAYER,

        GET_ALL,

        GET_BY_NAME,

        GET_BY_UUID

    }
}
