package cloud.hytora.driver.networking.protocol.packets.defaults;

import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.NetworkComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DriverLoggingPacket extends AbstractPacket {

    private NetworkComponent component;
    private String message;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                component = buf.readObject(SimpleNetworkComponent.class);
                message = buf.readString();
                break;

            case WRITE:
                buf.writeObject(component);
                buf.writeString(message);
                break;
        }
    }
}
