package cloud.hytora.driver.services.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@Getter
public class ServiceUpdateNametagsPacket extends AbstractPacket {

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
