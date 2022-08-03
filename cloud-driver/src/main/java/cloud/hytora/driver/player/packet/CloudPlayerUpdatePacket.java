package cloud.hytora.driver.player.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.driver.player.ICloudPlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudPlayerUpdatePacket extends AbstractPacket {

    private ICloudPlayer player;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                player = buf.readObject(DefaultCloudPlayer.class);
                break;

            case WRITE:
                buf.writeObject(player);
                break;
        }
    }
}
