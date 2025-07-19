package cloud.hytora.driver.networking.protocol;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;

import java.io.IOException;

public interface PacketTypeProcessor {

    IPacket readPacket(PacketBuffer buffer) throws IOException;

    void writePacket(IPacket packet, PacketBuffer buffer);
}
