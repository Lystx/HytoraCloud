package cloud.hytora.driver.networking.packets.services;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CloudServerCommandPacket extends AbstractPacket {

    private String command;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                command = buf.readString();
                break;

            case WRITE:
                buf.writeString(command);
                break;
        }
    }
}
