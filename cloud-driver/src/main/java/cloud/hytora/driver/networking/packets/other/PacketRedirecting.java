package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketRedirecting extends AbstractPacket {


    public static PacketRedirecting forAll(IPacket packet) {
        return new PacketRedirecting("ALL", packet);
    }

    private String client;
    private IPacket packet;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                client = buf.readString();
                packet = buf.readPacket();
                break;

            case WRITE:
                buf.writeString(client);
                buf.writePacket(packet);
                break;
        }
    }
}
