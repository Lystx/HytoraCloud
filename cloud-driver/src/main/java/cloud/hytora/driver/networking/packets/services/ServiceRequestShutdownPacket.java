package cloud.hytora.driver.networking.packets.services;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * The receiver of this {@link Packet} (Only Node makes sense) checks if a server
 * with the provided name of this packet is registered and if so shuts it down
 *
 * @author Lystx
 * @see ServiceForceShutdownPacket
 * @since SNAPSHOT-1.0
 */
@NoArgsConstructor @AllArgsConstructor @Getter
public class ServiceRequestShutdownPacket extends Packet {

    /**
     * The name of the service that should be shut down
     */
    private String service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                service = buf.readString();
                break;

            case WRITE:
                buf.writeString(service);
                break;
        }
    }

}
