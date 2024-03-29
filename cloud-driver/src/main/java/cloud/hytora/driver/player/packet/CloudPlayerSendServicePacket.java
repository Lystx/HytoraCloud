package cloud.hytora.driver.player.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CloudPlayerSendServicePacket extends AbstractPacket {

    private UUID uuid;
    private String service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                uuid = buf.readUniqueId();
                service = buf.readString();
                break;

            case WRITE:
                buf.writeUniqueId(uuid);
                buf.writeString(service);
                break;
        }
    }
}
