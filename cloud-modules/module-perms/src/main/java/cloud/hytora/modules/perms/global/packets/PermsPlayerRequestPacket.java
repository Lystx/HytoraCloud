package cloud.hytora.modules.perms.global.packets;

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
public class PermsPlayerRequestPacket extends AbstractPacket {

    private String name;
    private UUID uniqueId;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeOptionalString(name);
                buf.writeOptionalUniqueId(uniqueId);
                break;
            case READ:
                this.name = buf.readOptionalString();
                this.uniqueId = buf.readOptionalUniqueId();
        }
    }
}
