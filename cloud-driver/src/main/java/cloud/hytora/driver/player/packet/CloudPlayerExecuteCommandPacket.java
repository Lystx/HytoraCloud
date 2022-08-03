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
public class CloudPlayerExecuteCommandPacket extends AbstractPacket {

    /**
     * The uuid of the player that executed a command
     */
    private UUID uuid;

    /**
     * The commandLine that was executed
     */
    private String commandLine;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                uuid = buf.readUniqueId();
                commandLine = buf.readString();
                break;

            case WRITE:
                buf.writeUniqueId(uuid);
                buf.writeString(commandLine);
                break;
        }
    }
}
