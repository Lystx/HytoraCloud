package cloud.hytora.driver.networking.packets.player;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.player.impl.SimpleCloudPlayer;
import cloud.hytora.driver.player.CloudPlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudPlayerUpdatePacket extends Packet {

    private CloudPlayer player;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                player = buf.readObject(SimpleCloudPlayer.class);
                break;

            case WRITE:
                buf.writeObject(player);
                break;
        }
    }
}
