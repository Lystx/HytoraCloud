package cloud.hytora.driver.networking.packets.player;

import cloud.hytora.driver.component.ChatComponent;
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
@NoArgsConstructor
@Getter
public class CloudPlayerComponentMessagePacket extends Packet {

    private UUID uuid;
    private ChatComponent message;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                uuid = buf.readUniqueId();
                message = buf.readObject(ChatComponent.class);
                break;

            case WRITE:
                buf.writeUniqueId(uuid);
                buf.writeObject(message);
                break;
        }
    }
}
