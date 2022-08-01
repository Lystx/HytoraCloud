package cloud.hytora.driver.networking.packets.services;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * The receiver of this {@link Packet} checks if the name of its Driver
 * matches the requested "service" name and if it matches will then shut down the server
 *
 * @author Lystx
 * @see ServiceRequestShutdownPacket
 * @since SNAPSHOT-1.0
 */
@Getter @AllArgsConstructor @NoArgsConstructor
public class ServiceForceShutdownPacket extends AbstractPacket {

    /**
     * The name of the service to shut down
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
