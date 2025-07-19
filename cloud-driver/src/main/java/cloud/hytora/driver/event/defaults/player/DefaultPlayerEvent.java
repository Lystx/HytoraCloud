package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.player.CloudPlayer;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public abstract class DefaultPlayerEvent implements LocalEvent, NetworkEvent {

    protected CloudPlayer cloudPlayer;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                cloudPlayer = buf.readPlayer();
                break;

            case WRITE:
                buf.writePlayer(cloudPlayer);
                break;
        }
    }





    public static DefaultPlayerEvent forLogin(CloudPlayer cloudPlayer) {
        return new CloudEventPlayerLoginSuccess(cloudPlayer);
    }

}
