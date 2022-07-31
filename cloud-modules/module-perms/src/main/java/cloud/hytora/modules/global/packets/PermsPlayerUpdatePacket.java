package cloud.hytora.modules.global.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermsPlayerUpdatePacket extends AbstractPacket {

    private PermissionPlayer player;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        if (state == BufferState.READ) {
            player = buf.readObject(DefaultPermissionPlayer.class);
        } else {
            buf.writeObject(player);
        }
    }
}
