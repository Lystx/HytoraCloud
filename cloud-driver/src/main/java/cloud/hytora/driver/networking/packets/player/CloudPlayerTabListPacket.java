package cloud.hytora.driver.networking.packets.player;

import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudPlayerTabListPacket extends AbstractPacket {

    private UUID uuid;
    private String header, footer;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                uuid = buf.readUniqueId();
                header = buf.readString();
                footer = buf.readString();
                break;

            case WRITE:
                buf.writeUniqueId(uuid);
                buf.writeString(header);
                buf.writeString(footer);
                break;
        }
    }
}
