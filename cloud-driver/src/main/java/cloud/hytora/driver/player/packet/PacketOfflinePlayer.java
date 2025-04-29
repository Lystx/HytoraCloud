package cloud.hytora.driver.player.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

import static cloud.hytora.driver.player.packet.PacketOfflinePlayer.PayLoad.*;


public class PacketOfflinePlayer extends AbstractPacket {

    public PacketOfflinePlayer() {
        super(buf -> buf.writeEnum(GET_ALL));
    }

    public PacketOfflinePlayer(CloudOfflinePlayer savedPlayer) {
        super(buf -> buf.writeEnum(SAVE_PLAYER).writeObject(savedPlayer));
    }

    public PacketOfflinePlayer(String name) {
        super(buf -> buf.writeEnum(GET_BY_NAME).writeString(name));
    }

    public PacketOfflinePlayer(UUID uniqueID) {
        super(buf -> buf.writeEnum(GET_BY_UUID).writeUniqueId(uniqueID));
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
