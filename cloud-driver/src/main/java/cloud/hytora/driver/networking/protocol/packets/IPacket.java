package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

public interface IPacket extends IBufferObject {


    PacketTransferInfo transferInfo();

    PacketBuffer buffer();


    Task<Void> publishAsync();

    void setDestinationChannel(String destinationChannel);

    String getDestinationChannel();

    void publish();

    void publishTo(String... receivers);

    Task<BufferedResponse> awaitResponse();
}
