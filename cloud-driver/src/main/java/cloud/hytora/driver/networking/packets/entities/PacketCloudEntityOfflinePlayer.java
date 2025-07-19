package cloud.hytora.driver.networking.packets.entities;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

import static cloud.hytora.driver.networking.packets.entities.PacketCloudEntityOfflinePlayer.PayLoad.*;


public class PacketCloudEntityOfflinePlayer extends AbstractPacket {

    public PacketCloudEntityOfflinePlayer() {
        super(buf -> buf.writeEnum(GET_ALL));
    }

    public PacketCloudEntityOfflinePlayer(CloudOfflinePlayer savedPlayer) {
        super(buf -> buf.writeEnum(SAVE_PLAYER).writeObject(savedPlayer));
    }

    public PacketCloudEntityOfflinePlayer(CloudOfflinePlayer savedPlayer, boolean updateToCache) {
        super(buf -> buf.writeEnum(UPDATE_TO_CACHE).writeObject(savedPlayer));
    }

    public PacketCloudEntityOfflinePlayer(String name) {
        super(buf -> buf.writeEnum(GET_BY_NAME).writeString(name));
    }

    public PacketCloudEntityOfflinePlayer(UUID uniqueID) {
        super(buf -> buf.writeEnum(GET_BY_UUID).writeUniqueId(uniqueID));
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
    }

    public enum PayLoad {

        UPDATE_TO_CACHE,

        SAVE_PLAYER,

        GET_ALL,

        GET_BY_NAME,

        GET_BY_UUID

    }
}
