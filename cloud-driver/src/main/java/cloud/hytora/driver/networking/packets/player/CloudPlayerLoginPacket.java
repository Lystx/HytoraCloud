package cloud.hytora.driver.networking.packets.player;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CloudPlayerLoginPacket extends Packet {

    private String username;
    private UUID uuid;

    private String proxy;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                username = buf.readString();
                uuid = buf.readUniqueId();
                proxy = buf.readString();
                break;

            case WRITE:
                buf.writeString(username);
                buf.writeUniqueId(uuid);
                buf.writeString(proxy);
                break;
        }
    }
}
