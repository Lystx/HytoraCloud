package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

public interface IPacket extends Bufferable {

    PacketTransferInfo transferInfo();

    PacketBuffer buffer();

    void setDestinationChannel(String channelName);

    String getDestinationChannel();
}
